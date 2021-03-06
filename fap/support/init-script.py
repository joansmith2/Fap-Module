#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import subprocess
import platform
import getopt
import os
from subprocess import Popen, PIPE
import httplib # Necesaria para hacer peticiones HTTP

platform = {'WinOS':True if platform.system() == 'Windows' else False}

def executePlayCommand(args, error):
  cmd = args[:]
  cmd.insert(0, playCmd())    
  ret = subprocess.call(cmd)
  if ret != 0:
    raise Exception(error) 

def playCmd():
  cmd = "play"
  if platform['WinOS']:
    cmd = "play.bat"
  return cmd  

def checkPlay():
  """Comprueba que play este instalado""" 
  #TODO falta hacer esta comprobación
  print "[checks] - Comprobando que play está instalado"
  try:
      Popen([playCmd()], stdout=PIPE, stderr=PIPE)
  except Exception as error:
      print "[checks] - Play no está instalado :("
      raise Exception(error)
  print "[checks] - Play! está instalado"
  return False

def createPlayApp(appname):
  """Crea la aplicación play"""
  executePlayCommand(['new', appname], 'La aplicación play no se creó correctamente')  

def configureDeps(appname):
  """Configura las dependencias de la aplicación """
  #Escribe el fichero de dependencias
  depsfile = os.path.join(appname, 'conf', 'dependencies.yml')
  f = open(depsfile, 'w')
  
  try: #Descargar las dependencies del servidor
      # Configuración
      dominio = 'fap-devel.etsii.ull.es'
      ruta_fichero = '/public/Dependencies/dependencies.yml'  
      # conectamos con el servidor
      conn = httplib.HTTPConnection(dominio)
      # hacemos la petición del fichero de dependencias
      conn.request ("GET", '/' + ruta_fichero)
      r = conn.getresponse()
      depsfile = os.path.join(appname, 'conf', 'dependencies.yml')
      f = open(depsfile, 'w')
      # guardamos el fichero de dependencias en nuestro fichero
      f.write(r.read())
      print "[dependencies] - Dependencias descargadas del servidor!"
  except: # Si no se ha podido tirar del servidor, creamos unas dependencies por defecto
      print "[dependencies] - No se ha podido descargar las dependencias del servidor :("
      print "[dependencies] - Creando las dependencias por defecto ... "
      # Este contenido debería estar siempre actualizado con la última versión, en este caso sería porque la
      # última versión no se ha podido descargar desde el servidor
      depscontent = """# Dependencias
require:
    - play
    - fap -> fap 1.1.1
    - play -> pdf 0.6
    - play -> recaptcha 1.2
# Custom repository 
repositories: 
    - Fap repository:
        type: http
        artifact: "http://fap-devel.etsii.ull.es/public/fap-sdk2/[revision]/[module]-[revision].zip"
        contains:
            - fap -> fap"""
      f.write(depscontent)
  f.close()
  executePlayCommand(['deps', appname], 'La aplicación play no se creó correctamente')  

def fapinit(appname):
  """Ejecuta el comando fap:init y genera"""
  executePlayCommand(['fap:init', appname], 'La aplicación fap no se inicializó correctamente')    
  executePlayCommand(['fap:generate', appname], 'La aplicación fap no se generó correctamente')    

def parseParams(args):
  """Parsea los parámetros de la línea de comandos""" 
  optlist = []
  newargs = []
  try:
    optlist, newargs = getopt.getopt(args[1:], 'x', ['name=', 'path='])
  except getopt.GetoptError:
      pass

  appname = ""
  if len(newargs) == 0:
    raise Exception('No se especifico el nombre de la aplicación')  
  else:
     appname = newargs[0]

  return optlist, appname

def main(*args):
  print "Software Factory Fap"
  print "--------------------"
  try :
    checkPlay()
      
    args, appname = parseParams(args)
    createPlayApp(appname)
    configureDeps(appname)
    fapinit(appname)

    print "~ [SUCCESS] - La aplicación se inicializó correctamente"
    print "~ Para arranzar la aplicación ejecuta: play run", appname
    print "~ Y acccede con tu navegador a http://localhost:9000"
  except Exception as e:
    print "~ [Error]", e



if __name__ == '__main__':
    sys.exit(main(*sys.argv))
