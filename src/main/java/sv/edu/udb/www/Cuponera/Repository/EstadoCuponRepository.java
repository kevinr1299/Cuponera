package sv.edu.udb.www.Cuponera.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sv.edu.udb.www.Cuponera.Entities.EstadoCupon;

@Repository("EstadoCuponRepository")
public interface EstadoCuponRepository extends JpaRepository<EstadoCupon, Integer> {

}
