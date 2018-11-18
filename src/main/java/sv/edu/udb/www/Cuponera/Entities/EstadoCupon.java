package sv.edu.udb.www.Cuponera.Entities;
// Generated 11-17-2018 03:23:21 PM by Hibernate Tools 4.3.1


import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * EstadoCupon generated by hbm2java
 */
@Entity
@Table(name="estado_cupon"
    ,catalog="Cuponera"
)
public class EstadoCupon  implements java.io.Serializable {


     private Integer id;
     private String descripcion;
     private Set<Cupon> cupons = new HashSet<Cupon>(0);

    public EstadoCupon() {
    }

	
    public EstadoCupon(String descripcion) {
        this.descripcion = descripcion;
    }
    public EstadoCupon(String descripcion, Set<Cupon> cupons) {
       this.descripcion = descripcion;
       this.cupons = cupons;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)

    
    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    
    @Column(name="descripcion", nullable=false, length=150)
    public String getDescripcion() {
        return this.descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="estadoCupon")
    public Set<Cupon> getCupons() {
        return this.cupons;
    }
    
    public void setCupons(Set<Cupon> cupons) {
        this.cupons = cupons;
    }




}


