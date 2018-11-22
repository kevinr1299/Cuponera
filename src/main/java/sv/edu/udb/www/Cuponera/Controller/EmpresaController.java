package sv.edu.udb.www.Cuponera.Controller;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
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
import sv.edu.udb.www.Cuponera.Repository.EmpresaRepository;
import sv.edu.udb.www.Cuponera.Repository.RolRepository;
import sv.edu.udb.www.Cuponera.Repository.RubroRepository;
import sv.edu.udb.www.Cuponera.Repository.TipoRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;
import sv.edu.udb.www.Cuponera.Service.EmailService;
import sv.edu.udb.www.Cuponera.Utils.PasswordGenerator;

@Controller
@PreAuthorize("hasAuthority('Administrador')")
@RequestMapping("/empresa")
public class EmpresaController {

	@Autowired
	@Qualifier("EmailService")
	EmailService emailService;
	
	@Autowired
	@Qualifier("EmpresaRepository")
	EmpresaRepository empresaRepository;
	
	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;
	
	@Autowired
	@Qualifier("TipoRepository")
	TipoRepository tipoRepository;
	
	@Autowired
	@Qualifier("RolRepository")
	RolRepository rolRepository;
	
	@Autowired
	@Qualifier("RubroRepository")
	RubroRepository rubroRepository;
	
	@Autowired
	@Qualifier("EmpleadoRepository")
	EmpleadoRepository empleadoRepository;
	
	@GetMapping()
	public String listaEmpresas(Model model) {
		model.addAttribute("lista", empresaRepository.findAll());
		return "Empresa/Lista";
	}
	
	@GetMapping("/nuevo")
	public String nuevaEmpresa(Model model) {
		model.addAttribute("empresa", new Empresa());
		model.addAttribute("rubros", rubroRepository.findAll());
		return "Empresa/Nuevo";
	}
	
	@PostMapping("/nuevo")
	public String guardarEmpresa(@Valid @ModelAttribute("empresa") Empresa empresa, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(result.hasErrors()) {
				model.addAttribute("empresa", empresa);
				model.addAttribute("rubros", rubroRepository.findAll());
				return "Empresa/Nuevo";
			}else if(!empresa.getCorreo().matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")) {
				result.addError(new ObjectError("correo","Formato de correo incorrecto"));
				model.addAttribute("empresa", empresa);
				model.addAttribute("rubros", rubroRepository.findAll());
				return "Empresa/Nuevo";
			}else if(!(empresa.getComision() >= 1 && empresa.getComision() <= 100)) {
				result.addError(new ObjectError("comision","La comision debe estar entre 1 y 100 porciento"));
				model.addAttribute("empresa", empresa);
				model.addAttribute("rubros", rubroRepository.findAll());
				return "Empresa/Nuevo";
			}else {
				String clave = PasswordGenerator.getPassword(PasswordGenerator.MINUSCULAS + PasswordGenerator.MAYUSCULAS, 10);
				Usuario user = crearUsuario(empresa.getCorreo(), clave);
				int empresaId = empresaRepository.getCount();
				String codigo = "";
				if(empresaId < 10) {
					codigo = "EMP00" + empresaId;
				}else if(empresaId < 100) {
					codigo = "EMP0" + empresaId;
				}else if(empresaId > 100) {
					codigo = "EMP" + empresaId;
				}
				Empresa e = empresaRepository.findByIdentificador(codigo);
				if(e != null) {
					int max = empresaRepository.getMaxId();
					if(max < 10) {
						codigo = "EMP00" + max;
					}else if(max < 100) {
						codigo = "EMP0" + max;
					}else if(max > 100) {
						codigo = "EMP" + max;
					}
				}
				empresa.setIdentificador(codigo);
				if(empresaRepository.save(empresa) != null) {
					if(usuarioRepository.save(user) != null) {
						emailService.sendSimpleMessage(user.getCorreo(), "Ingreso a la cuponera", "La cuponera le da la bienvenida, las credenciales con las que podra ingresar por primera vez son: " + clave);
						int usuarioId = usuarioRepository.getMaxId();
						empresaId = empresaRepository.getMaxId();
						Empleado emp = crearEmpleado(empresa.getNombre(), usuarioId, empresaId);
						empleadoRepository.save(emp);
						atributos.addFlashAttribute("exito","Empresa ingresada");
						return "redirect:/empresa";
					}
				}
				model.addAttribute("empresa", empresa);
				model.addAttribute("rubros", rubroRepository.findAll());
				return "Empresa/Nuevo";
			}
		}catch(MessagingException ex) {
			result.addError(new ObjectError("correo","Ocurrio un problema enviando el correo, contactenos por medio de correo o telefono para brindarle una solucion"));
			model.addAttribute("empresa", empresa);
			model.addAttribute("rubros", rubroRepository.findAll());
			return "Empresa/Nuevo";
		}catch(Exception ex) {
			result.addError(new ObjectError("correo","El correo ingresado ya esta ocupado"));
			model.addAttribute("empresa", empresa);
			model.addAttribute("rubros", rubroRepository.findAll());
			return "Empresa/Nuevo";
		}
	}
	
	@GetMapping("/{codigo}/actualizar")
	public String obtenerEmpresa(@PathVariable("codigo") int codigo, Model model) {
		Empresa empresa = empresaRepository.getOne(codigo);
		Usuario usuario = new Usuario();
		for(Empleado emp : empresa.getEmpleados()){
			if(emp.getRol().getId() == 1) {
				usuario = emp.getUsuario();
				break;
			}
		}
		empresa.setCorreo(usuario.getCorreo());
		model.addAttribute("empresa", empresa);
		model.addAttribute("rubros", rubroRepository.findAll());
		return "Empresa/Modificar";
	}
	
	@PostMapping("/modificar")
	public String actualizarEmpresa(@Valid @ModelAttribute("empresa") Empresa empresa, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(result.hasErrors()) {
				model.addAttribute("empresa", empresa);
				model.addAttribute("rubros", rubroRepository.findAll());
				return "Empresa/Modificar";
			}else if(!(empresa.getComision() >= 1 && empresa.getComision() <= 100)) {
				result.addError(new ObjectError("comision","La comision debe estar entre 1 y 100 porciento"));
				model.addAttribute("empresa", empresa);
				model.addAttribute("rubros", rubroRepository.findAll());
				return "Empresa/Modificar";
			}else {
				empresaRepository.save(empresa);
				atributos.addFlashAttribute("exito", "Empresa actualizada");
				return "redirect:/empresa";
			}
		}catch(Exception ex) {
			System.out.println(ex);
			model.addAttribute("empresa", empresa);
			model.addAttribute("rubros", rubroRepository.findAll());
			return "Empresa/Modificar";
		}
	}
	
	@GetMapping("{codigo}/eliminar")
	public String eliminarEmpresa(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		try {
			Empresa empresa = empresaRepository.getOne(codigo);
			if(empresa.getPromocions().size() == 0) {
				for(Empleado emp : empresa.getEmpleados()) {
					empleadoRepository.delete(emp);
					usuarioRepository.delete(emp.getUsuario());
				}
				empresaRepository.delete(empresa);
				atributos.addFlashAttribute("exito", "Empresa elimiada");
			}else {
				atributos.addFlashAttribute("fallo", "No se pudo eliminar la empresa");
			}
		}catch(Exception ex) {
			atributos.addFlashAttribute("fallo", "No se pudo eliminar la empresa");
		}
		return "redirect:/empresa";
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
	
	private Empleado crearEmpleado(String nombre, int usuario, int empresa) {
		Empleado emp = new Empleado();
		emp.setNombre(nombre);
		emp.setApellido(nombre);
		emp.setRol(rolRepository.getOne(1));
		emp.setEmpresa(empresaRepository.getOne(empresa));
		emp.setUsuario(usuarioRepository.getOne(usuario));
		return emp;
	}
}
