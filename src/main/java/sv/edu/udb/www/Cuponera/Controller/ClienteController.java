package sv.edu.udb.www.Cuponera.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sv.edu.udb.www.Cuponera.Entities.Cliente;
import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.ClienteRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;
import sv.edu.udb.www.Cuponera.Utils.CambioClave;

@Controller()
@PreAuthorize("hasAuthority('Cliente')")
@RequestMapping("/cliente")
public class ClienteController {
	
	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;
	
	@Autowired
	@Qualifier("ClienteRepository")
	ClienteRepository clienteRepository;
	
	@GetMapping("/actualizar")
	public String obtenerCliente(Model model) {
		Cliente cliente = obtenerCliente();
		if(cliente == null) {
			return "/login";
		}
		model.addAttribute("cliente", cliente);
		return "General/Modificar";
	}
	
	@GetMapping("/clave")
	public String formCambio(Model model) {
		Cliente cliente = obtenerCliente();
		if(cliente == null) {
			return "/login";
		}
		model.addAttribute("cliente", cliente);
		model.addAttribute("cambio",new CambioClave());
		return "General/clave";
	}
	
	@PostMapping("/actualizar")
	public String actualizarCliente(HttpServletRequest request, @Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			Cliente u = obtenerCliente();
			if(u.getId() != cliente.getId()) {
				result.addError(new ObjectError("id","La informacion del cliente que actualiza no coincide con el usuario en sesion"));
			}
			if(cliente.getTarjeta().isEmpty() && cliente.getPin().isEmpty()) {
				if(!cliente.getClave().trim().matches("^[0-9]{16}$")) {
					result.addError(new ObjectError("tarjeta","Formato de tarjeta incorreto (################)"));
				}
				if(!cliente.getConfirmacion().trim().matches("^[0-9]{3}$")) {
					result.addError(new ObjectError("pin","Formato de PIN incorreto (###)"));
				}
				if(result.hasErrors()) {
					model.addAttribute("cliente", cliente);
					return "General/Modificar";
				}else {
					cliente.setUsuario(u.getUsuario());
					String pin = DigestUtils.sha256Hex(cliente.getConfirmacion());
					String tarjeta = DigestUtils.sha256Hex(cliente.getClave());
					cliente.setPin(pin);
					cliente.setTarjeta(tarjeta);
					clienteRepository.save(cliente);
					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
					atributos.addFlashAttribute("exito", "Datos modificados, vuelva a iniciar sesion");
					return "redirect:/logout";
				}
			}else if(!cliente.getClave().isEmpty() || !cliente.getConfirmacion().isEmpty()) {
				if(!cliente.getClave().trim().matches("^[0-9]{16}$")) {
					result.addError(new ObjectError("tarjeta","Formato de tarjeta incorreto (################)"));
				}
				if(!cliente.getConfirmacion().trim().matches("^[0-9]{3}$")) {
					result.addError(new ObjectError("pin","Formato de PIN incorreto (###)"));
				}
				if(result.hasErrors()) {
					model.addAttribute("cliente", cliente);
					return "General/Modificar";
				}else {
					cliente.setUsuario(u.getUsuario());
					String pin = DigestUtils.sha256Hex(cliente.getConfirmacion());
					String tarjeta = DigestUtils.sha256Hex(cliente.getClave());
					cliente.setPin(pin);
					cliente.setTarjeta(tarjeta);
					clienteRepository.save(cliente);
					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
					atributos.addFlashAttribute("exito", "Datos modificados, vuelva a iniciar sesion");
					return "redirect:/logout";
				}
			}else {
				if(result.hasErrors()) {
					model.addAttribute("cliente", cliente);
					return "General/Modificar";
				}else {
					cliente.setUsuario(u.getUsuario());
					clienteRepository.save(cliente);
					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
					atributos.addFlashAttribute("exito", "Datos modificados, vuelva a iniciar sesion");
					return "redirect:/logout";
				}
			}
		}catch(Exception ex) {
			System.out.println(ex);
			model.addAttribute("cliente", cliente);
			return "General/Modificar";
		}
	}
	
	@PostMapping("/clave")
	public String cambiarClave(HttpServletRequest request, @ModelAttribute("cambio") CambioClave cambio, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			Cliente cliente = obtenerCliente();
			Usuario user = usuarioRepository.getOne(cliente.getUsuario().getId());
			String hexOriginal = DigestUtils.sha256Hex(cambio.getOriginal());
			String hexClave = DigestUtils.sha256Hex(cambio.getClave());
			if(!hexOriginal.equals(user.getPassword())) {
				result.addError(new ObjectError("original","El contraseña ingresada no coincide"));
			}
			if(!cambio.getClave().matches("^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{8,16}$")) {
				result.addError(new ObjectError("clave","La contraseña debe tener al entre 8 y 16 caracteres, al menos un dígito, al menos una minúscula y al menos una mayúscula"));
			}
			if(!cambio.getClave().trim().equals(cambio.getConfirmacion().trim())) {
				result.addError(new ObjectError("confirmacion","La nueva contraseña y la confirmación no coinciden"));
			}
			if(result.hasErrors()) {
				model.addAttribute("cliente", cliente);
				model.addAttribute("cambio", cambio);
				return "General/clave";
			}else {
				user.setPassword(hexClave);
				usuarioRepository.save(user);
				atributos.addFlashAttribute("exito", "Contraseña actualizada");
				return "redirect:/index";
			}
		}catch(Exception ex) {
			atributos.addFlashAttribute("error","No se pudo actualizar");
			return "redirect:/cliente/clave";
		}
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
			cliente.setCorreo(user.getCorreo());
			return cliente;
		}catch(Exception ex) {
			return null;
		}
	}
	
}
