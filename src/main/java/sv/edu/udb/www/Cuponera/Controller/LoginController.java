package sv.edu.udb.www.Cuponera.Controller;

import java.util.Date;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sv.edu.udb.www.Cuponera.Entities.Cliente;
import sv.edu.udb.www.Cuponera.Entities.Empleado;
import sv.edu.udb.www.Cuponera.Entities.Rol;
import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.ClienteRepository;
import sv.edu.udb.www.Cuponera.Repository.PromocionRepository;
import sv.edu.udb.www.Cuponera.Repository.TipoRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;
import sv.edu.udb.www.Cuponera.Service.EmailService;
import sv.edu.udb.www.Cuponera.Utils.CambioClave;
import sv.edu.udb.www.Cuponera.Utils.PasswordGenerator;


@Controller
public class LoginController {
	
	@Autowired
	@Qualifier("EmailService")
	EmailService email;
	
	@Autowired
	@Qualifier("ClienteRepository")
	ClienteRepository clienteRepository;
	
	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;
	
	@Autowired
	@Qualifier("PromocionRepository")
	PromocionRepository promocionRepository;
	
	@Autowired
	@Qualifier("TipoRepository")
	TipoRepository tipoRepository;
	
	@GetMapping("/login")
	public String showFormLogin() {
		return "/login";
	}
	
	@Autowired
	HttpServletRequest context;
	
	@GetMapping("/registro")
	public String showFormRegistro(Model model) {
		model.addAttribute("cliente", new Cliente());
		return "General/Registro";
	}
	
	@PostMapping("/registro")
	public String ingresarCliente(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(!cliente.getCorreo().matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")){
				result.addError(new ObjectError("correo","Formato de correo incorrecto"));
			}
			if(!cliente.getClave().matches("^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{8,16}$")) {
				result.addError(new ObjectError("clave","La contraseña debe tener al entre 8 y 16 caracteres, al menos un dígito, al menos una minúscula y al menos una mayúscula"));
			}
			if(cliente.getConfirmacion().isEmpty()) {
				result.addError(new ObjectError("confirmacion","La confirmacion es obligatoria"));
			}
			if(result.hasErrors()) {
				model.addAttribute("cliente", cliente);
				return "General/Registro";
			}else {
				if(cliente.getClave().trim().equals(cliente.getConfirmacion().trim())) {
					if(cliente.getTarjeta().trim() != "" && cliente.getPin().trim() == "") {
						result.addError(new ObjectError("pin","Si ingresa la tarjeta debe ingresar el pin"));
					}else if(cliente.getTarjeta().trim() == "" && cliente.getPin().trim() != ""){
						result.addError(new ObjectError("tarjeta","Si ingresa el pin debe ingresar la tarjeta"));
					}else if(cliente.getTarjeta().trim() == "" && cliente.getPin().trim() == "") {
						Usuario user = crearUsuario(cliente.getCorreo(), cliente.getClave());
						if(usuarioRepository.save(user) != null) {
							int idUsuario = usuarioRepository.getMaxId();
							cliente.setUsuario(usuarioRepository.getOne(idUsuario));
							if(clienteRepository.save(cliente) != null) {
								email.sendMimeMessage(user.getCorreo(), "Verificación", "La cuponera te da la bienvenida a su plataforma online, para verificar tu cuenta <a href='http://" + context.getServerName() + ":" + context.getServerPort() + "/verificar/" + user.getToken() + "'>click aquí</a>");
								atributos.addFlashAttribute("exito", "Verifique el usuario para ingresar");
								return "redirect:/login";
							}
						}
					}else {
						if(!cliente.getTarjeta().trim().matches("^[0-9]{16}$")) {
							result.addError(new ObjectError("tarjeta","Formato de tarjeta incorreto (################)"));
						}else if(!cliente.getPin().trim().matches("^[0-9]{3}$")) {
							result.addError(new ObjectError("pin","Formato de PIN incorreto (###)"));
						}else {
							Usuario user = crearUsuario(cliente.getCorreo(), cliente.getClave());
							if(usuarioRepository.save(user) != null) {
								int idUsuario = usuarioRepository.getMaxId();
								cliente.setUsuario(usuarioRepository.getOne(idUsuario));
								String pin = DigestUtils.sha256Hex(cliente.getPin());
								String tarjeta = DigestUtils.sha256Hex(cliente.getTarjeta());
								cliente.setPin(pin);
								cliente.setTarjeta(tarjeta);
								if(clienteRepository.save(cliente) != null) {
									email.sendMimeMessage(user.getCorreo(), "Verificación", "La cuponera te da la bienvenida a su plataforma online, para verificar tu cuenta <a href='http://" + context.getServerName() + ":" + context.getServerPort() + "/verificar/" + user.getToken() + "'>click aquí</a>");
									atributos.addFlashAttribute("exito", "Verifique el usuario para ingresar");
									return "redirect:/login";
								}
							}
						}
					}
				}else {
					result.addError(new ObjectError("confirmacion","La contraseña y la confirmación no coinciden"));
				}
				model.addAttribute("cliente", cliente);
				return "General/Registro";
			}
		}catch(MessagingException ex) {
			result.addError(new ObjectError("correo","Ocurrio un problema enviando el correo, contactenos por medio de correo o telefono para brindarle una solucion"));
			model.addAttribute("cliente", cliente);
			return "General/Registro";
		}
		catch(Exception ex) {
			result.addError(new ObjectError("correo","El correo ingresado ya esta ocupado"));
			model.addAttribute("cliente", cliente);
			return "General/Registro";
		}
	}
	
	@GetMapping("/verificar/{token}")
	public String verificar(@PathVariable("token") String token, RedirectAttributes atributos) {
		try {
			Usuario usuario = usuarioRepository.findByToken(token);
			usuario.setVerificado(true);
			usuarioRepository.save(usuario);
			atributos.addFlashAttribute("exito", "Usuario verificado");
		}catch(Exception ex) {
			System.out.println("Error verificando");
		}
		return "redirect:/login";
	}
	
	@GetMapping("/index")
	public String index(Model model) {
		model.addAttribute("lista", promocionRepository.listaActivos(new Date()));
		model.addAttribute("cliente", obtenerCliente());
		return "General/index";
	}
	
	@GetMapping("/redireccion")
	public String redirigir(HttpServletRequest request, Model model) {
		try {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			UserDetails userDetails = null;
			if (principal instanceof UserDetails) {
				userDetails = (UserDetails) principal;
			}
			Usuario user = usuarioRepository.findByCorreo(userDetails.getUsername());
			if(user.isInicio()) {
				switch(user.getTipoUsuario().getId()) {
				case 1:
					return "redirect:/oferta/admin";
				case 2:
					return "redirect:/index";
				case 3:
					Rol rol = new Rol();
					for(Empleado emp : user.getEmpleados()) {
						rol = emp.getRol();
						break;
					}
					if(rol.getId() == 1) {
						return "redirect:/oferta";
					}else {
						return "Cupon/Canje";
					}
				}
				request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
				return "redirect:/logout";
			}else {
				model.addAttribute("cambio", new CambioClave());
				return "General/Inicio";
			}
		}catch(Exception ex) {
			request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
			return "redirect:/logout";
		}
	}
	
	@PostMapping("/clave")
	public String cambiarClave(HttpServletRequest request, @ModelAttribute("cambio") CambioClave cambio, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			String hexClave = DigestUtils.sha256Hex(cambio.getClave());
			if(!cambio.getClave().matches("^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{8,16}$")) {
				result.addError(new ObjectError("clave","La contraseña debe tener al entre 8 y 16 caracteres, al menos un dígito, al menos una minúscula y al menos una mayúscula"));
			}
			if(!cambio.getClave().trim().equals(cambio.getConfirmacion().trim())) {
				result.addError(new ObjectError("confirmacion","La nueva contraseña y la confirmación no coinciden"));
			}
			if(result.hasErrors()) {
				model.addAttribute("cambio", cambio);
				return "General/Inicio";
			}else {
				Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				UserDetails userDetails = null;
				if (principal instanceof UserDetails) {
					userDetails = (UserDetails) principal;
				}
				Usuario user = usuarioRepository.findByCorreo(userDetails.getUsername());
				user.setPassword(hexClave);
				user.setInicio(true);
				usuarioRepository.save(user);
				atributos.addFlashAttribute("exito","Contraseña actualizada");
				return "redirect:/redireccion";
			}
		}catch(Exception ex) {
			System.out.println(ex);
			request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
			return "redirect:/logout";
		}
	}
	
	@GetMapping("/recuperar/{codigo}")
	public String recuperar(@PathVariable("codigo") String correo, RedirectAttributes atributos) throws MessagingException {
		Usuario user = usuarioRepository.findByCorreo(correo);
		if(user == null) {
			atributos.addFlashAttribute("fallo","No se encontro ningun usuario con ese correo");
		}else {
			String clave = PasswordGenerator.getPassword(PasswordGenerator.MINUSCULAS + PasswordGenerator.MAYUSCULAS, 10);
			user.setPassword(DigestUtils.sha256Hex(clave));
			user.setInicio(false);
			usuarioRepository.save(user);
			email.sendSimpleMessage(user.getCorreo(), "Recuperar contraseña", "Su contraseña para ingresar es: " + clave + ", al ingresar debera actualizar su contraseña" );
		}
		return "redirect:/login";
	}
	
	private Usuario crearUsuario(String correo, String clave) {
		Usuario user = new Usuario();
		user.setCorreo(correo);
		user.setPassword(DigestUtils.sha256Hex(clave));
		user.setVerificado(false);
		user.setTipoUsuario(tipoRepository.getOne(2));
		user.setToken(UUID.randomUUID().toString());
		user.setInicio(true);
		return user;
	}
	
	private Cliente obtenerCliente() {
		try {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			UserDetails userDetails = null;
			if (principal instanceof UserDetails) {
				userDetails = (UserDetails) principal;
			}
			Usuario user = usuarioRepository.findByCorreo(userDetails.getUsername());
			Cliente cliente = new Cliente();
			for(Cliente c : user.getClientes()) {
				cliente = c;
				break;
			}
			return cliente;
		}catch(Exception ex) {
			return null;
		}
	}
}
