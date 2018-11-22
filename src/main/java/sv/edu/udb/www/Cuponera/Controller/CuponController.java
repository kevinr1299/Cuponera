package sv.edu.udb.www.Cuponera.Controller;

import java.util.Date;

import javax.mail.MessagingException;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sv.edu.udb.www.Cuponera.Entities.Cliente;
import sv.edu.udb.www.Cuponera.Entities.Cupon;
import sv.edu.udb.www.Cuponera.Entities.Promocion;
import sv.edu.udb.www.Cuponera.Entities.Usuario;
import sv.edu.udb.www.Cuponera.Repository.CuponRepository;
import sv.edu.udb.www.Cuponera.Repository.EstadoCuponRepository;
import sv.edu.udb.www.Cuponera.Repository.EstadoOfertaRepository;
import sv.edu.udb.www.Cuponera.Repository.PromocionRepository;
import sv.edu.udb.www.Cuponera.Repository.UsuarioRepository;
import sv.edu.udb.www.Cuponera.Service.EmailService;

@Controller()
@RequestMapping("/cupon")
public class CuponController {
	
	@Autowired
	@Qualifier("EmailService")
	EmailService email;
	
	@Autowired
	@Qualifier("PromocionRepository")
	PromocionRepository promocionRepository;
	
	@Autowired
	@Qualifier("UsuarioRepository")
	UsuarioRepository usuarioRepository;
	
	@Autowired
	@Qualifier("EstadoCuponRepository")
	EstadoCuponRepository estadoCuponRepository;
	
	@Autowired
	@Qualifier("EstadoOfertaRepository")
	EstadoOfertaRepository estadoOfertaRepository;
	
	@Autowired
	@Qualifier("CuponRepository")
	CuponRepository cuponRepository;
	
	@GetMapping("/{pin}/{codigo}/comprar")
	public String comprarCupon(@PathVariable("pin") String pin, @PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		boolean bandera = true;
		try {
			Promocion promocion = promocionRepository.getOne(codigo);
			if(promocion != null) {
				Cliente cliente = obtenerCliente();
				String hexPin = DigestUtils.sha256Hex(pin);
				if(hexPin.equals(cliente.getPin())) {
					if(promocion.getCuponesDisponibles() != 0) {
						if(promocion.getEstadoOferta().getId() == 1) {
							String id ="";
							int random = 0;
							while(bandera) {
								random = (int) (Math.random() * 9999999) + 1;
								if(random < 10) {
									id = promocion.getEmpresa().getIdentificador() + "000000" + random;
								}else if(random < 100) {
									id = promocion.getEmpresa().getIdentificador() + "00000" + random;
								}else if(random < 1000) {
									id = promocion.getEmpresa().getIdentificador() + "0000" + random;
								}else if(random < 10000) {
									id = promocion.getEmpresa().getIdentificador() + "000" + random;
								}else if(random < 100000) {
									id = promocion.getEmpresa().getIdentificador() + "00" + random;
								}else if(random < 1000000) {
									id = promocion.getEmpresa().getIdentificador() + "0" + random;
								}else if(random < 10000000) {
									id = promocion.getEmpresa().getIdentificador() + random;
								}
								bandera = cuponRepository.existsById(id);
							}
							Cupon cupon = new Cupon();
							cupon.setCliente(cliente);
							cupon.setPromocion(promocion);
							cupon.setFechaCompra(new Date());
							cupon.setFechaCanje(new Date());
							cupon.setEstadoCupon(estadoCuponRepository.getOne(1));
							cupon.setId(id);
							cuponRepository.save(cupon);
							email.sendSimpleMessage(cliente.getUsuario().getCorreo(), "Compra de cupon", "Felicidades por su compra, para poder canjear su cupon presentese en un local, este correo (digital o impreso) junto a su DUI, el encargado pedira su numero de DUI y numero de cupon para verificar el cambio, el numero de su cupon es: " + id + ", tiene hasta el: " + promocion.getFechaLimite() + " para canjear su cupon");
							atributos.addFlashAttribute("exito", "Cupon comprado");
							int disponibles = promocion.getCuponesDisponibles() - 1;
							promocion.setCuponesDisponibles(disponibles);
							return "redirect:/index";
						}else {
							atributos.addFlashAttribute("fallo","La oferta ya no esta disponible");
							return "redirect:/index";
						}
					}else {
						atributos.addFlashAttribute("fallo","La promocion ya no tiene cupones disponibles");
						return "redirect:/index";
					}
				}else {
					atributos.addFlashAttribute("fallo","El pin no coincide con el guardado");
					return "redirect:/index";
				}
			}else {
				atributos.addFlashAttribute("fallo","No se encontro la oferta solicitada");
				return "redirect:/index";
			}
		}catch(MessagingException ex) {
			atributos.addFlashAttribute("fallo","Se compro el cupon; pero ocurrio un error enviando el correo");
			return "redirect:/index";
		}catch(Exception ex) {
			atributos.addFlashAttribute("fallo","Ocurrio un error comprando el cupon");
			return "redirect:/index";
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
