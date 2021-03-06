package models;

import javax.persistence.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.util.Date;

import java.util.Set;
import java.util.HashSet;

import play.data.format.*;
import play.Logger;


@Entity
public class Tarea{
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;
  private String titulo;
  private Boolean terminada;
  private String descripcion;
  //Relación muchos-a-uno entre tareas y usuario
  @ManyToOne
  //Nombre de la columna en la BD que guarda físicamente
  //ei ID del usuario con el que está asociado una tarea
  @JoinColumn(name="usuarioId")
  public Usuario usuario;
  //Relación muchos-a-uno entre tareas y usuario
  @ManyToOne
  @JoinColumn(name="tableroId")
  public Tablero tablero;
  @ManyToOne
  @JoinColumn(name="usuarioTareaId")
  public Usuario responsable;

  // Variable para guardar fecha creación tarea
  private LocalDateTime fechaCreacion;
  @Formats.DateTime(pattern="dd-MM-yyyy")
  @Temporal(TemporalType.DATE)
  private Date fechaLimite;
  @ManyToMany(fetch=FetchType.EAGER)
  @JoinTable(name="Etiqueta_Tarea")
  private Set<Etiqueta> etiquetas = new HashSet<Etiqueta>();
  //Relación uno-a-muchos entre usuario y comentario
  @OneToMany(mappedBy="tarea", fetch=FetchType.EAGER)
  public Set<Comentario> comentarios=new HashSet<Comentario>();


  public Tarea() {}

  public Tarea(Usuario usuario,String titulo){
    try{
      SimpleDateFormat formateador=new SimpleDateFormat("dd-MM-yyyy");
      Date fechaaux=formateador.parse("01-01-1900");
      this.usuario=usuario;
      this.titulo=titulo;
      this.fechaCreacion=LocalDateTime.now();
      this.descripcion = "";
      this.fechaLimite=fechaaux;
      this.terminada=false;
    }catch (Exception e) {}
  }
  //Getters y setters necesarios para JPA

  public Long getId(){
    return id;
  }

  public void setId(Long id){
    this.id=id;
  }

  public String getTitulo(){
    return titulo;
  }

  public void setTitulo(String titulo){
    this.titulo=titulo;
  }

  public Usuario getUsuario(){
    return usuario;
  }

  public void setUsuario(Usuario usuario){
    this.usuario=usuario;
  }

  public Boolean getTerminada(){
    return terminada;
  }

  public void setTerminada(Boolean terminada){
    this.terminada=terminada;
  }

  public LocalDateTime getFechaCreacion(){
    return fechaCreacion;
  }

  public Date getFechaLimite(){
    Date fecDefecto=new Date();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    try{
      fecDefecto=sdf.parse("01-01-1900");
      return fechaLimite==null ? fecDefecto: fechaLimite;
    }catch (Exception e) { return fecDefecto;}
  }

  public void setFechaLimite(Date fechaLimite){
    this.fechaLimite=fechaLimite;
  }

  public String getDescripcion(){
    return this.descripcion;
  }
  public void setDescripcion(String descripcion){
    if(descripcion == null){
      this.descripcion = "";
    }else{
      this.descripcion=descripcion;
    }
  }

  public Tablero getTablero(){
    return this.tablero;
  }

  public void setTablero(Tablero tablero){
    this.tablero=tablero;
  }

  public Set<Etiqueta> getEtiquetas() {
    return etiquetas;
  }

  public void setEtiquetas(Set<Etiqueta> etiquetas) {
    this.etiquetas = etiquetas;
  }


  public Set<Comentario> getComentarios(){
    return comentarios;
  }

  public void setComentarios(Set<Comentario> comentarios){
    this.comentarios=comentarios;
}
  public Usuario getResponsable(){
    return responsable;
  }

  public void setResponsable(Usuario usuario){
    this.responsable=usuario;

  }

  public boolean tareaCaducada(){
    Date fechaHoy=new Date();
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
    Date fecDefecto=new Date();
    try{
      fecDefecto=sdf.parse("01-01-1900");
      return (fechaLimite==null || fechaLimite.equals(fecDefecto)) ? false : fechaHoy.after(this.getFechaLimite());
    }catch (Exception e) { return false;}
  }

  public String toString(){
    return String.format("Tarea id: %s titulo: %s usuario:%s",
                    id,titulo,usuario.toString());
  }

  @Override
  public int hashCode(){
    final int prime=31;
    int result=prime+((titulo==null)?0:titulo.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (getClass() != obj.getClass()) return false;
    Tarea other = (Tarea) obj;
    // Si tenemos los ID, comparamos por ID
    if (id != null && other.id != null)
      return ((long)id == (long)other.id);
    // sino comparamos por campos obligatorios
    else {
       if (titulo == null) {
          if (other.titulo != null) return false;
       } else if (!titulo.equals(other.titulo)) return false;
       if (usuario == null) {
        if (other.usuario != null) return false;
        else if (!usuario.equals(other.usuario)) return false;
        }
        if (fechaCreacion == null) {
         if (other.fechaCreacion != null) return false;
         else if (!fechaCreacion.equals(other.fechaCreacion)) return false;
         }
      }
      return true;
   }
}
