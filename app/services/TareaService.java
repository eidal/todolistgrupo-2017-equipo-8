package services;

import javax.inject.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Set;
import java.util.HashSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Calendar;

import models.Usuario;
import models.UsuarioRepository;
import models.Tablero;
import models.TableroRepository;
import models.Tarea;
import models.TareaRepository;
import models.Etiqueta;
import models.EtiquetaRepository;

import models.Comentario;
import models.ComentarioRepository;

import play.Logger;


public class TareaService{
  UsuarioRepository usuarioRepository;
  TareaRepository tareaRepository;
  TableroRepository tableroRepository;
  EtiquetaRepository etiquetaRepository;
  ComentarioRepository comentarioRepository;

  @Inject
  public TareaService(UsuarioRepository usuarioRepository,TareaRepository tareaRepository, TableroRepository tableroRepository,EtiquetaRepository etiquetaRepository, ComentarioRepository comentarioRepository){
    this.usuarioRepository=usuarioRepository;
    this.tareaRepository=tareaRepository;
    this.tableroRepository=tableroRepository;
    this.etiquetaRepository=etiquetaRepository;
    this.comentarioRepository = comentarioRepository;
  }

  //Devuelve la lista de tareas de un usuario, ordenadas por su id
  //(equivalente al orden de creación)
  public List<Tarea> allTareasUsuario(Long idUsuario){
    Usuario usuario=usuarioRepository.findById(idUsuario);
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }
    List<Tarea> tareas=new ArrayList<Tarea>(usuario.getTareas());
    List<Tarea> result = new ArrayList<Tarea>();
    for(Tarea task: tareas){
        if( task.getTablero()==null){
          result.add(task);
        }
    }
    Collections.sort(result,(a,b) -> a.getId() < b.getId() ? -1 : a.getId()==b.getId() ? 0 : 1);
    return result;
  }

  public List<Tarea> tareasTerminadas(Long idUsuario){
    Usuario usuario=usuarioRepository.findById(idUsuario);
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }
    List<Tarea> tareas=new ArrayList<Tarea>(usuario.getTareas());
    List<Tarea> result = new ArrayList<Tarea>();
    for(Tarea task: tareas){
        if(task.getTerminada()){
          result.add(task);
        }
    }
    Collections.sort(result,(a,b) -> a.getId() > b.getId() ? -1 : a.getId()==b.getId() ? 0 : 1);
    return result;
  }

  //Devuelve la lista de tareas de un usuario, ordenadas por su fecha límite
  public List<Tarea> allTareasUsuarioOrdenadasFechaLimite(Long idUsuario){
    Usuario usuario=usuarioRepository.findById(idUsuario);
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }
    List<Tarea> tareas=new ArrayList<Tarea>(usuario.getTareas());
    List<Tarea> result = new ArrayList<Tarea>();
    for(Tarea task: tareas){
        if(task.getTablero()==null){
          result.add(task);
        }
    }
    Collections.sort(result,(a,b) -> a.getFechaLimite().before(b.getFechaLimite()) ? -1 : a.getFechaLimite().equals(b.getFechaLimite()) ? 0 : 1);
    return result;
  }

  public Tarea nuevaTarea(Long idUsuario, String titulo, String fechaLimite, String descripcion){
    Usuario usuario= usuarioRepository.findById(idUsuario);
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }
    Tarea tarea=new Tarea(usuario,titulo);
    if (fechaLimite != null){
      try{
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        Date fechaLim=sdf.parse(fechaLimite);
        Date fecdefecto=sdf.parse("01-01-1900");
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        Date fechaAyer= cal.getTime();
        if (fechaAyer.after(fechaLim) && (!(fechaLim.equals(fecdefecto)))){
          throw new TareaServiceException("No se puede establecer una fecha límite inferior a hoy");
        }
        tarea=new Tarea(usuario,titulo);
        tarea.setFechaLimite(fechaLim);
      } catch (Exception e){
        throw new TareaServiceException("La fecha introducida es incorrecta, debe ser del tipo dd-MM-yyyy");
      }
    }
    if (descripcion != null){
      tarea.setDescripcion(descripcion);
    }

    return tareaRepository.add(tarea);
  }

  public Tarea obtenerTarea(Long idTarea){
    return tareaRepository.findById(idTarea);
  }

  public Tarea modificaTarea(Long idTarea,String nuevoTitulo, String fechaLimite, String nuevaDescripcion){
    Tarea tarea=tareaRepository.findById(idTarea);
    if(tarea==null)
      throw new TareaServiceException("No existe tarea");

    tarea.setTitulo(nuevoTitulo);
    tarea.setDescripcion(nuevaDescripcion);

    if(fechaLimite!=null){
      try{
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        Date fechaLim=sdf.parse(fechaLimite);
        Date fecdefecto=sdf.parse("01-01-1900");
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        Date fechaAyer= cal.getTime();
        if (fechaAyer.after(fechaLim) && (!(fechaLim.equals(fecdefecto)))){
          throw new TareaServiceException("La fecha introducida es incorrecta, debe ser como mínimo igual al día de hoy");
        }
        tarea.setFechaLimite(fechaLim);
      } catch (Exception e){
        throw new TareaServiceException("La fecha introducida es incorrecta, debe ser del tipo dd-MM-yyyy");
      }
    }
    tarea=tareaRepository.update(tarea);
    return tarea;
  }

  public void borraTarea(Long idTarea){
    Tarea tarea=tareaRepository.findById(idTarea);
    if(tarea==null)
      throw new TareaServiceException("No existe tarea");
    Set<Comentario> comentarios= tarea.getComentarios();
    if (comentarios.size()>0){
      for (Comentario comentario : comentarios ) {
        comentarioRepository.delete(comentario.getId());
      }
    }
    tareaRepository.delete(idTarea);
  }

  public void tareaTerminada(Long idTarea){
    Tarea tarea=tareaRepository.findById(idTarea);
    if(tarea==null)
      throw new TareaServiceException("No existe tarea");
    tarea.setTerminada(true);
    tareaRepository.update(tarea);
  }

  public void reactivarTareaTerminada(Long idTarea){
    Tarea tarea=tareaRepository.findById(idTarea);
    if(tarea==null)
      throw new TareaServiceException("No existe tarea");
    tarea.setTerminada(false);
    tareaRepository.update(tarea);
  }


  public Tarea addEtiquetaATarea(Long idTarea, Long idEtiqueta){
    Tarea tarea = tareaRepository.findById(idTarea);
    if (tarea==null){
      throw new TareaServiceException("Error. Tarea no existente");
    }
    Etiqueta etiqueta= etiquetaRepository.findById(idEtiqueta);
    if (etiqueta==null){
      throw new TareaServiceException("Error. Etiqueta no existente");
    }
    Set<Etiqueta> etiquetas=tarea.getEtiquetas();
    Set<Tarea> tareas=etiqueta.getTareas();
    Tablero tablero=tarea.getTablero();
    if(tablero!=null){
      if(tablero.getEtiquetas().contains(etiqueta)){
        etiquetas.add(etiqueta);
        tarea.setEtiquetas(etiquetas);
        tareas.add(tarea);
        etiqueta.setTareas(tareas);
        tarea=tareaRepository.update(tarea);
        etiquetaRepository.update(etiqueta);
      }
      else{
        throw new TareaServiceException("Error. La etiqueta no pertenece al tablero de la tarea");
      }
    }
    else{
      if(tarea.getUsuario().getEtiquetas().contains(etiqueta)){
        etiquetas.add(etiqueta);
        tarea.setEtiquetas(etiquetas);
        tareas.add(tarea);
        etiqueta.setTareas(tareas);
        tarea=tareaRepository.update(tarea);
        etiquetaRepository.update(etiqueta);
      }
      else{
        throw new TareaServiceException("Error. La etiqueta no pertenece al usuario de la tarea");
      }
    }
    return tarea;
  }


  public Tarea borraEtiquetaATarea(Long idTarea, Long idEtiqueta){
    Tarea tarea = tareaRepository.findById(idTarea);
    if (tarea==null){
      throw new TareaServiceException("Error. Tarea no existente");
    }
    Etiqueta etiqueta= etiquetaRepository.findById(idEtiqueta);
    if (etiqueta==null){
      throw new TareaServiceException("Error. Etiqueta no existente");
    }
    Set<Etiqueta> etiquetas=tarea.getEtiquetas();
    Set<Tarea> tareas=etiqueta.getTareas();
    boolean borrado=etiquetas.remove(etiqueta);
    if(borrado){
      tarea.setEtiquetas(etiquetas);
      tareas.remove(tarea);
      etiqueta.setTareas(tareas);
      tarea=tareaRepository.update(tarea);
      etiquetaRepository.update(etiqueta);
      return tarea;
    }
    else {
      throw new TareaServiceException("Error, la etiqueta a borrar no pertenece a la tarea");
    }
  }

  public Tarea modificaEtiquetaATarea(Long idTarea, Long idEtiqueta,String color,String nombre){
    Tarea tarea = tareaRepository.findById(idTarea);
    if (tarea==null){
      throw new TareaServiceException("Error. Tarea no existente");
    }
    Etiqueta etiqueta= etiquetaRepository.findById(idEtiqueta);
    if (etiqueta==null){
      throw new TareaServiceException("Error. Etiqueta no existente");
    }
    Set<Etiqueta> etiquetas=tarea.getEtiquetas();
    if(etiquetas.contains(etiqueta)){
      try{
        if(color!=null){
          etiqueta.setColor(color);
        }
        if(nombre!=null){
          etiqueta.setNombre(nombre);
        }
        etiquetaRepository.update(etiqueta);
      } catch (IllegalArgumentException e){
        throw new TareaServiceException("Error, el color no es válido");
      }
    }
    else{
      throw new TareaServiceException("Error, la etiqueta a modificar no pertenece a la tarea");
    }
    tarea = tareaRepository.findById(idTarea);
    return tarea;
  }

  //Devuelve las etiquetas en una lista ordenada por color y nombre
  public List<Etiqueta> allEtiquetasTarea(Long idTarea){
    Tarea tarea=tareaRepository.findById(idTarea);
    if(tarea==null){
      throw new TareaServiceException("Tarea no existente");
    }
    List<Etiqueta> etiquetas=new ArrayList<Etiqueta>(tarea.getEtiquetas());
    Collections.sort(etiquetas,(a,b) -> (a.getColor().compareTo(b.getColor())<0 || (a.getColor().equals(b.getColor()) && a.getNombre().compareTo(b.getNombre())<0)) ? -1 : (a.getColor().equals(b.getColor()) && a.getNombre().equals(b.getNombre())) ? 0 : 1);
    return etiquetas;
  }

  //Devuelve las etiquetas pendientes de insertar en una lista ordenada por color y nombre
  public List<Etiqueta> allEtiquetasTareaSinAsignarDisponibles(Long idTarea){
    List<Etiqueta> disponibles;
    Tarea tarea=tareaRepository.findById(idTarea);
    if(tarea==null){
      throw new TareaServiceException("Tarea no existente");
    }
    Tablero tablero=tarea.getTablero();
    if(tablero!=null){
      disponibles=new ArrayList<Etiqueta>(tablero.getEtiquetas());
    }
    else{
      disponibles=new ArrayList<Etiqueta>(tarea.getUsuario().getEtiquetas());
    }
    List<Etiqueta> etiquetas=new ArrayList<Etiqueta>(tarea.getEtiquetas());
    //elimino todas las etiquetas ya usadas en la tarea
    disponibles.removeAll(etiquetas);
    Collections.sort(etiquetas,(a,b) -> (a.getColor().compareTo(b.getColor())<0 || (a.getColor().equals(b.getColor()) && a.getNombre().compareTo(b.getNombre())<0)) ? -1 : (a.getColor().equals(b.getColor()) && a.getNombre().equals(b.getNombre())) ? 0 : 1);
    return disponibles;
  }

  public boolean EtiquetaPerteneceTarea(Long idTarea,String color, String nombre){
    Tarea tarea = tareaRepository.findById(idTarea);
    if (tarea==null){
      throw new TareaServiceException("Error. Tarea no existente");
    }
    Set<Etiqueta> etiquetas=tarea.getEtiquetas();
    return etiquetas.stream().filter(etiqueta -> etiqueta.getColor().equals(color) && etiqueta.getNombre().equals(nombre)).count()>0;
  }

  public Tarea addResponsableTarea(Long idTarea, Long idUsuario){
    Tarea tarea = tareaRepository.findById(idTarea);
    Usuario usuario= usuarioRepository.findById(idUsuario);
    if (tarea==null){
      throw new TareaServiceException("Error. Tarea no existente");
    }
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }

    Set<Tarea> tareas=usuario.getTareasAsig();
    tareas.add(tarea);
    usuario.setTareasAsig(tareas);
    tarea.setResponsable(usuario);
    tarea=tareaRepository.update(tarea);
    usuarioRepository.modify(usuario);
    return tarea;
  }

  public Tarea borrarResponsableTarea(Long idTarea, Long idUsuario){
    Tarea tarea = tareaRepository.findById(idTarea);
    Usuario usuario= usuarioRepository.findById(idUsuario);
    if (tarea==null){
      throw new TareaServiceException("Error. Tarea no existente");
    }
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }
    Set<Tarea> tareas=usuario.getTareasAsig();
    if(tareas.remove(tarea)){
      usuario.setTareasAsig(tareas);
      tarea.setResponsable(null);
      tarea=tareaRepository.update(tarea);
      usuarioRepository.modify(usuario);
      return tarea;
    }else{
      throw new TareaServiceException("Error al borrar el responsable");
    }


  }

  public List<Tarea> allTareasResponsable(Long idUsuario){
    Usuario usuario=usuarioRepository.findById(idUsuario);
    if(usuario==null){
      throw new TareaServiceException("Usuario no existente");
    }
    List<Tarea> tareas=new ArrayList<Tarea>(usuario.getTareasAsig());
    Collections.sort(tareas,(a,b) -> a.getId() < b.getId() ? -1 : a.getId()==b.getId() ? 0 : 1);
    return tareas;
  }

  public List<Tarea> filtradoTareas(Long idTablero, Long idUsuario, List<Etiqueta> etiquetas){
    List<Tarea> filtradas=new ArrayList<Tarea>();
    if(idTablero!=0){
      Tablero tablero = tableroRepository.findById(idTablero);
      if (tablero==null){
        throw new TareaServiceException("Error. Tablero no existente");
      }
      List<Tarea> tareas = new ArrayList<Tarea>(tablero.getTareas());
      for(Tarea tarea : tareas){
        List<Etiqueta> etiqTarea = new ArrayList<Etiqueta>(allEtiquetasTarea(tarea.getId()));
        int numEtiqTarea = etiqTarea.size();
        etiqTarea.removeAll(etiquetas);
        if(etiqTarea.size()<numEtiqTarea){
          filtradas.add(tarea);
        }
      }
    }
    else {
      Usuario usuario=usuarioRepository.findById(idUsuario);
      if(usuario==null){
        throw new TareaServiceException("Error. Usuario no existente");
      }
      List<Tarea> tareas = new ArrayList<Tarea>(usuario.getTareas());
      for(Tarea tarea : tareas){
        List<Etiqueta> etiqTarea = new ArrayList<Etiqueta>(allEtiquetasTarea(tarea.getId()));
        int numEtiqTarea = etiqTarea.size();
        etiqTarea.removeAll(etiquetas);
        if(etiqTarea.size()<numEtiqTarea){
          filtradas.add(tarea);
        }
      }
    }
    return filtradas;
  }
}
