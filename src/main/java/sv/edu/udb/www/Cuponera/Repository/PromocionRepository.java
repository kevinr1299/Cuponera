package sv.edu.udb.www.Cuponera.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sv.edu.udb.www.Cuponera.Entities.Empresa;
import sv.edu.udb.www.Cuponera.Entities.EstadoOferta;
import sv.edu.udb.www.Cuponera.Entities.Promocion;

@Repository("PromocionRepository")
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

	public abstract List<Promocion> findByEmpresa(Empresa empresa);
	public abstract List<Promocion> findByEstadoOferta(EstadoOferta estado);
}
