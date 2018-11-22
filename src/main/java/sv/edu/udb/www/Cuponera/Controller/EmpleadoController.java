package sv.edu.udb.www.Cuponera.Controller;

import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sv.edu.udb.www.Cuponera.Entities.Empleado;
import sv.edu.udb.www.Cuponera.Entities.Empresa;
import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.EmpleadoRepository;
import sv.edu.udb.www.Cuponera.Repository.RolRepository;
import sv.edu.udb.www.Cuponera.Repository.TipoRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;
import sv.edu.udb.www.Cuponera.Service.EmailService;
import sv.edu.udb.www.Cuponera.Utils.PasswordGenerator;

@Controller()
@PreAuthorize("hasAuthority('Empresa')")
@RequestMapping("/empleado")
public class EmpleadoController {

	@Autowired
	@Qualifier("EmailService")
	EmailService emailService;
	
	@Autowired
	@Qualifier("EmpleadoRepository")
	EmpleadoRepository empleadoRepository;
	
	@Autowired
	@Qualifier("RolRepository")
	RolRepository rolRepository;
	
	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;
	
	@Autowired
	@Qualifier("TipoRepository")
	TipoRepository tipoRepository;
	
	@GetMapping()
	public String listaEmpleados(Model model) {
		Empresa empresa = obtenerEmpresa();
		model.addAttribute("lista", empleadoRepository.findByEmpresa(empresa));
		return "Empleado/Lista";
	}
	
	@GetMapping("/nuevo")
	public String nuevoEmpleado(Model model) {
		model.addAttribute("empleado", new Empleado());
		return "Empleado/Nuevo";
	}
	
	@PostMapping("/nuevo")
	public String guardarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(!empleado.getCorreo().matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")){
				result.addError(new ObjectError("correo","Formato de correo incorrecto"));
			}
			if(result.hasErrors()) {
				model.addAttribute("empleado", empleado);
				return "Empleado/Nuevo";
			}else {
				String clave = PasswordGenerator.getPassword(PasswordGenerator.MINUSCULAS + PasswordGenerator.MAYUSCULAS, 10);
				Usuario user = crearUsuario(empleado.getCorreo(), clave);
				if(usuarioRepository.save(user) != null) {
					emailService.sendSimpleMessage(empleado.getCorreo(), "Ingreso a la cuponera", "La cuponera le da la bienvenida, las credenciales con las que podra ingresar por primera vez son: " + clave);
					int usuarioId = usuarioRepository.getMaxId();
					empleado.setRol(rolRepository.getOne(2));
					empleado.setUsuario(usuarioRepository.getOne(usuarioId));
					empleado.setEmpresa(obtenerEmpresa());
					empleadoRepository.save(empleado);
					atributos.addFlashAttribute("exito","Empleado ingresado");
					return "redirect:/empleado";
				}
				result.addError(new ObjectError("correo","No se pudo ingresar el usuario del empleado"));
				model.addAttribute("empleado", empleado);
				return "Empleado/Nuevo";
			}
			
		}catch(Exception ex) {
			result.addError(new ObjectError("correo","El correo ingresado ya esta ocupado"));
			model.addAttribute("empleado", empleado);
			return "Empleado/Nuevo";
		}
	}
	
	@GetMapping("/{codigo}/actualizar")
	public String obtenerEmpleado(@PathVariable("codigo") int codigo, Model model) {
		model.addAttribute("empleado", empleadoRepository.getOne(codigo));
		return "Empleado/Modificar";
	}
	
	@PostMapping("/actualizar")
	public String actualizarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(result.hasErrors()) {
				model.addAttribute("empleado", empleado);
				return "Empleado/Modificar";
			}else {
				empleado.setRol(rolRepository.getOne(2));
				empleado.setUsuario(empleadoRepository.getOne(empleado.getId()).getUsuario());
				empleado.setEmpresa(obtenerEmpresa());
				empleadoRepository.save(empleado);
				atributos.addFlashAttribute("exito", "Empleado actualizado");
				return "redirect:/empleado";
			}
			
		}catch(Exception ex) {
			result.addError(new ObjectError("correo","El correo ingresado ya esta ocupado"));
			model.addAttribute("empleado", empleado);
			return "Empleado/Nuevo";
		}
	}
	
	@GetMapping("/{codigo}/eliminar")
	public String eliminarEmpleado(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		try {
			Empleado empleado = empleadoRepository.getOne(codigo);
			Usuario user = empleado.getUsuario();
			empleadoRepository.delete(empleado);
			usuarioRepository.delete(user);
			atributos.addFlashAttribute("exito", "Empleado eliminado");
		}catch(Exception ex) {
			atributos.addFlashAttribute("fallo","No se pudo eliminar el empleado");
		}
		return "redirect:/empleado";
	}
	
	private Usuario crearUsuario(String correo, String clave) {
		Usuario user = new Usuario();
		user.setCorreo(correo);
		user.setPassword(DigestUtils.sha256Hex(clave));
		user.setVerificado(true);
		user.setTipoUsuario(tipoRepository.getOne(3));
		user.setToken("12345");
		user.setInicio(false);
		return user;
	}
	
	private Empresa obtenerEmpresa() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserDetails userDetails = null;
		if (principal instanceof UserDetails) {
			userDetails = (UserDetails) principal;
		}
		Usuario user = usuarioRepository.findByCorreo(userDetails.getUsername());
		Empresa empresa = new Empresa();
		for (Empleado emp : user.getEmpleados()) {
			empresa = emp.getEmpresa();
		}
		return empresa;
	}
}
