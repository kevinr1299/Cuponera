package sv.edu.udb.www.Cuponera.Controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.validation.Valid;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sv.edu.udb.www.Cuponera.Entities.Empleado;
import sv.edu.udb.www.Cuponera.Entities.Empresa;
import sv.edu.udb.www.Cuponera.Entities.Promocion;
import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.EstadoOfertaRepository;
import sv.edu.udb.www.Cuponera.Repository.PromocionRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;

@Controller()
@RequestMapping("/oferta")
public class PromocionController {

	public static String uploadDirectory = System.getProperty("user.dir") + "/uploads";

	@Autowired
	@Qualifier("PromocionRepository")
	PromocionRepository promocionRepository;

	@Autowired
	@Qualifier("EstadoOfertaRepository")
	EstadoOfertaRepository estadoRepository;

	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;

	@PreAuthorize("hasAuthority('Empresa')")
	@GetMapping()
	public String listaPromociones(Model model) {
		Empresa empresa = obtenerEmpresa();
		model.addAttribute("lista", promocionRepository.findByEmpresa(empresa));
		return "Promocion/Lista";
	}

	@PreAuthorize("hasAuthority('Empresa')")
	@GetMapping("/nuevo")
	public String formPromocion(Model model) {
		model.addAttribute("oferta", new Promocion());
		return "Promocion/Nuevo";
	}

	@PreAuthorize("hasAuthority('Empresa')")
	@PostMapping("/nuevo")
	public String guardarPromocion(@Valid @ModelAttribute("oferta") Promocion promocion,
			BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if (promocion.getFechaInicio() == null) {
				result.addError(new ObjectError("fechaInicio", "La fecha de inicio es obligatoria"));
			} else if (promocion.getFechaInicio().before(new Date())) {
				result.addError(new ObjectError("fechaInicio", "La fecha de inicio debe ser mayor a hoy"));
			}
			if (promocion.getFechaFin() == null) {
				result.addError(new ObjectError("fechaFin", "La fecha de finalizacion es obligatoria"));
			} else if (promocion.getFechaFin().before(promocion.getFechaInicio())) {
				result.addError(new ObjectError("fechaFin", "La echade finalizacion debe ser mayor a la de inicio"));
			}
			if (promocion.getFechaLimite() == null) {
				result.addError(new ObjectError("fechaLimite", "La fecha de limite de uso es obligatoria"));
			} else if (promocion.getFechaLimite().before(promocion.getFechaFin())) {
				result.addError(new ObjectError("fechaLimite", "La fecha limite debe ser mayor a la de finalizacion"));
			}
			if (promocion.getPrecioRegular() <= 0) {
				result.addError(new ObjectError("precioRegular", "El precio regular debe ser mayor a $0"));
			}
			if (promocion.getPrecioOferta() <= 0) {
				result.addError(new ObjectError("precioOferta", "El precio de oferta debe ser mayor a $0"));
			}
			if (promocion.getPrecioRegular() <= promocion.getPrecioOferta()) {
				result.addError(
						new ObjectError("precioRegular", "El precio regular debe ser menor al precio de oferta"));
			}
			if (!promocion.getDisponibles().equals("Ilimitados")) {
				if (!isInteger(promocion.getDisponibles())) {
					result.addError(new ObjectError("disponibles", "Los cupones disponibles deben ser un numero"));
				} else if (Integer.parseInt(promocion.getDisponibles()) <= 0) {
					result.addError(
							new ObjectError("disponibles", "El numero de cupones disponibles debe ser mayor a cero"));
				}
			}
			if(promocion.getFileData().getOriginalFilename() == null || promocion.getFileData().getOriginalFilename().isEmpty()) {
				result.addError(new ObjectError("fileData","La imagen es obligatoria"));
			}
			if (result.hasErrors()) {
				model.addAttribute("oferta", promocion);
				return "Promocion/Nuevo";
			} else {
				String img = uploadImage(promocion);
				if (img == null) {
					result.addError(new ObjectError("fileData", "No se pudo guardar la imagen"));
					model.addAttribute("oferta", promocion);
					return "Promocion/Nuevo";
				}
				if (promocion.getDisponibles().equals("Ilimitados")) {
					promocion.setCuponesDisponibles(-1);
				} else {
					promocion.setCuponesDisponibles(Integer.parseInt(promocion.getDisponibles()));
				}
				promocion.setEstadoOferta(estadoRepository.getOne(2));
				Empresa empresa = obtenerEmpresa();
				promocion.setEmpresa(empresa);
				promocion.setImg(img);
				promocionRepository.save(promocion);
				atributos.addFlashAttribute("exito", "Promocion ingresada");
				return "redirect:/oferta";
			}
		} catch (Exception ex) {
			result.addError(new ObjectError("titulo", "No se pudo guardar la oferta"));
			model.addAttribute("oferta", promocion);
			return "Promocion/Nuevo";
		}
	}

	@PreAuthorize("hasAuthority('Empresa')")
	@GetMapping("/{codigo}/actualizar")
	public String obtenerPromocion(@PathVariable("codigo") int codigo, Model model) {
		Promocion promo = promocionRepository.getOne(codigo);
		model.addAttribute("oferta", promo);
		model.addAttribute("url", uploadDirectory);
		return "Promocion/Modificar";
	}
	
	@PreAuthorize("hasAuthority('Empresa')")
	@PostMapping("/actualizar")
	public String actualizarPromocion(@Valid @ModelAttribute("oferta") Promocion promocion,
			BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if (promocion.getFechaInicio() == null) {
				result.addError(new ObjectError("fechaInicio", "La fecha de inicio es obligatoria"));
			} else if (promocion.getFechaInicio().before(new Date())) {
				result.addError(new ObjectError("fechaInicio", "La fecha de inicio debe ser mayor a hoy"));
			}
			if (promocion.getFechaFin() == null) {
				result.addError(new ObjectError("fechaFin", "La fecha de finalizacion es obligatoria"));
			} else if (promocion.getFechaFin().before(promocion.getFechaInicio())) {
				result.addError(new ObjectError("fechaFin", "La echade finalizacion debe ser mayor a la de inicio"));
			}
			if (promocion.getFechaLimite() == null) {
				result.addError(new ObjectError("fechaLimite", "La fecha de limite de uso es obligatoria"));
			} else if (promocion.getFechaLimite().before(promocion.getFechaFin())) {
				result.addError(new ObjectError("fechaLimite", "La fecha limite debe ser mayor a la de finalizacion"));
			}
			if (promocion.getPrecioRegular() <= 0) {
				result.addError(new ObjectError("precioRegular", "El precio regular debe ser mayor a $0"));
			}
			if (promocion.getPrecioOferta() <= 0) {
				result.addError(new ObjectError("precioOferta", "El precio de oferta debe ser mayor a $0"));
			}
			if (promocion.getPrecioRegular() <= promocion.getPrecioOferta()) {
				result.addError(
						new ObjectError("precioRegular", "El precio regular debe ser menor al precio de oferta"));
			}
			if (!promocion.getDisponibles().equals("Ilimitados")) {
				if (!isInteger(promocion.getDisponibles())) {
					result.addError(new ObjectError("disponibles", "Los cupones disponibles deben ser un numero"));
				} else if (Integer.parseInt(promocion.getDisponibles()) <= 0) {
					result.addError(
							new ObjectError("disponibles", "El numero de cupones disponibles debe ser mayor a cero"));
				}
			}
			if (result.hasErrors()) {
				model.addAttribute("oferta", promocion);
				return "Promocion/Modificar";
			} else {
				String img = promocion.getImg();
				if(!promocion.getFileData().getOriginalFilename().isEmpty()) {
					img = uploadImage(promocion);
					if (img == null) {
						result.addError(new ObjectError("fileData", "No se pudo guardar la imagen"));
						model.addAttribute("oferta", promocion);
						return "Promocion/Modificar";
					}
				}
				if (promocion.getDisponibles().equals("Ilimitados")) {
					promocion.setCuponesDisponibles(-1);
				} else {
					promocion.setCuponesDisponibles(Integer.parseInt(promocion.getDisponibles()));
				}
				promocion.setEstadoOferta(estadoRepository.getOne(2));
				Empresa empresa = obtenerEmpresa();
				promocion.setEmpresa(empresa);
				promocion.setImg(img);
				promocionRepository.save(promocion);
				atributos.addFlashAttribute("exito", "Promocion actualizada");
				return "redirect:/oferta";
			}
		} catch (Exception ex) {
			result.addError(new ObjectError("titulo", "No se pudo guardar la oferta"));
			model.addAttribute("oferta", promocion);
			return "Promocion/Modificar";
		}
	}
	
	@PreAuthorize("hasAuthority('Empresa')")
	@GetMapping("/{codigo}/eliminar")
	public String eliminarPromocion(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		try {
			Promocion promocion = promocionRepository.getOne(codigo);
			promocionRepository.delete(promocion);
			atributos.addFlashAttribute("exito","Promocion eliminada");
		}catch(Exception ex) {
			atributos.addFlashAttribute("fallo","No se puede eliminar la promocion");
		}
		return "redirect:/oferta";
	}
	
	@PreAuthorize("hasAuthority('Empresa')")
	@GetMapping("/{codigo}/mandar")
	public String adminPromocion(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		Promocion promocion = promocionRepository.getOne(codigo);
		promocion.setEstadoOferta(estadoRepository.getOne(2));
		promocionRepository.save(promocion);
		atributos.addFlashAttribute("exito","Estado de la promocion cambiado");
		return "redirect:/oferta";
	}
	
	@PreAuthorize("hasAuthority('Administrador')")
	@GetMapping("/admin")
	public String listaAdmin(Model model) {
		model.addAttribute("revision", promocionRepository.findByEstadoOferta(estadoRepository.getOne(2)));
		model.addAttribute("activas", promocionRepository.findByEstadoOferta(estadoRepository.getOne(1)));
		return "Promocion/Admin";
	}
	
	@PreAuthorize("hasAuthority('Administrador')")
	@GetMapping("/{codigo}/activar")
	public String activarPromocion(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		Promocion promocion = promocionRepository.getOne(codigo);
		promocion.setEstadoOferta(estadoRepository.getOne(1));
		promocionRepository.save(promocion);
		atributos.addFlashAttribute("exito","Estado de la promocion cambiado");
		return "redirect:/oferta/admin";
	}
	
	@PreAuthorize("hasAuthority('Administrador')")
	@GetMapping("/{codigo}/revisar")
	public String revisarPromocion(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		Promocion promocion = promocionRepository.getOne(codigo);
		promocion.setEstadoOferta(estadoRepository.getOne(3));
		promocionRepository.save(promocion);
		atributos.addFlashAttribute("exito","Estado de la promocion cambiado");
		return "redirect:/oferta/admin";
	}
	
	@PreAuthorize("hasAuthority('Administrador')")
	@GetMapping("/{codigo}/rechazar")
	public String rechazarPromocion(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		Promocion promocion = promocionRepository.getOne(codigo);
		promocion.setEstadoOferta(estadoRepository.getOne(4));
		promocionRepository.save(promocion);
		atributos.addFlashAttribute("exito","Estado de la promocion cambiado");
		return "redirect:/oferta/admin";
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

	private boolean isInteger(String numero) {
		try {
			Integer.parseInt(numero);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String uploadImage(Promocion promocion) {
		try {
			StringBuilder fileName = new StringBuilder();
			MultipartFile file = promocion.getFileData();
			Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
			fileName.append(file.getOriginalFilename());
			Files.write(fileNameAndPath, file.getBytes());
			return fileName.toString();
		} catch (Exception ex) {
			return null;
		}
	}
}
