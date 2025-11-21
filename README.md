# Taller 5 -  Secure Message Feed – Zero Trust Demo

Este proyecto implementa un sistema simple de registro de mensajes utilizando una arquitectura cliente–servidor con autenticación y autorización basada en Auth0, OAuth2, y JWT, aplicando los principios de Zero Trust vistos en clase.

## 1. Arquitectura


<img width="580" height="690" alt="Diagrama sin título drawio (3)" src="https://github.com/user-attachments/assets/bbad96c6-c55a-4a22-b1a1-cce11f416fc4" />


La aplicación se divide en dos componentes:

⸻

### 1. Cliente (Frontend – HTML + JS)

El cliente es una página web estática que:

	•	Permite ingresar un mensaje.
  
	•	Solicita al usuario pegar su token JWT emitido por Auth0.
  
	•	Envía llamadas POST y GET al backend usando fetch().
  
	•	Adjunta el token en el header HTTP:

Authorization: Bearer <token>

Importante:
El cliente no tiene lógica de autenticación propia. Solo envía el JWT proporcionado por Auth0. Esta separación evita exponer credenciales en el navegador.

⸻

### 2. Backend (Spring Boot + Spring Security)

El backend expone un API REST:

Método	Endpoint	Descripción

GET	/api/messages	Obtiene mensajes (requiere JWT)
POST	/api/messages	Guarda un mensaje (requiere JWT)

El backend implementa:

	•	oauth2ResourceServer para validar tokens JWT.
  
	•	Validación de audience, issuer, y firma del token.
  
	•	Política estricta:
  
toda ruta bajo /api/** requiere JWT obligatorio.

Además se habilita oauth2Login para que el navegador pueda autenticarse, pero esto no da acceso al API.
Solo el JWT con el audience correcto permite usar /api/messages.

⸻

### 3. Auth0 (Proveedor de Identidad)

Auth0 cumple los siguientes roles:

	•	Gestiona login de usuarios.
  
	•	Emite ID Tokens y Access Tokens (tipo JWT).
  
	•	El Access Token se emite con este audience:
https://secure-tutorial/api/

Este token es el que debe usarse en el frontend.

⸻

### 4. Flujo de Datos
	1.	El usuario entra al cliente HTML.
	2.	Da clic en “Login con Auth0” (por interfaz Auth0) o va a /oauth2/authorization/auth0.
	3.	Auth0 redirige al backend, el backend valida la identidad.
	4.	El usuario copia su Access Token desde Auth0.
	5.	Lo pega en el cliente HTML.
	6.	El cliente envía el token en cada petición al backend.
	7.	Spring Security verifica:
      	•	firma del JWT
      	•	iss (issuer)
      	•	aud (audience)
      	•	expiración
	8.	Si todo es válido → la API ejecuta la operación.
Si no → retorna 401 Unauthorized.

DIAGRAMA DEL PROCESO

<img width="486" height="325" alt="Captura de Pantalla 2025-11-20 a la(s) 7 45 36 p m" src="https://github.com/user-attachments/assets/ad601c14-3287-4b90-8662-1fdf5afc4c51" />  
<img width="477" height="230" alt="Captura de Pantalla 2025-11-20 a la(s) 7 48 35 p m" src="https://github.com/user-attachments/assets/9cb750f9-4273-4af7-95f9-583b51d98277" />

⸻

## 2. Cómo la arquitectura aplica Zero Trust

A continuación se explica cómo esta implementación real aplica los principios fundamentales de Zero Trust.

⸻

### 1. Nunca confiar, siempre verificar
	•	Ninguna petición al backend es aceptada por defecto.
	•	Cada llamada a /api/messages obliga a validar un JWT fresco.
	•	No se guarda ninguna sesión de confianza en backend.
	•	Aunque el usuario ya se autenticó por navegador, el API solo confía en el token, nunca en la sesión web.

⸻

### 2. Autenticación y autorización por solicitud

Cada request:
	•	Debe tener Authorization: Bearer <token>.
	•	El JWT se valida en cada petición.
	•	Si el token expiró → 401 inmediato.
	•	Si falta el audience → 401 inmediato.
	•	Si el token es ID Token en vez de Access Token → 401 inmediato.

No hay confianza previa entre cliente y servidor.

⸻

 ### 3. Mínima superficie de exposición
	•	Solo /api/messages/** está expuesto.
	•	El backend no expone paneles administrativos, formularios, dashboards, etc.
	•	Los recursos estáticos (/css, /js, /images) están permitidos, pero no contienen lógica sensible.
	•	Todo lo demás requiere autenticación.

⸻

### 4. Separación entre cliente público y API protegida

El cliente HTML:
	•	No tiene llaves, secretos ni credenciales.
	•	No maneja autenticación interna.
	•	Solo actúa como UI y reenvía el JWT que trae el usuario desde Auth0.

El backend:
	•	Es la única pieza que conoce la configuración de Auth0.
	•	Es la única capa que valida tokens.
	•	No confía en el frontend para seguridad.

Esto sigue el principio:
los clientes nunca deben tener secretos.

⸻

## 3. Reflexión sobre su alineación con principios de seguridad

La arquitectura se alinea con los principios estudiados así:

✔ Diseño orientado a Zero Trust

No se confía en la red, en la sesión ni en el dispositivo del usuario.
Cada solicitud debe autenticarse.

✔ Separación por capas

Frontend y backend cumplen roles distintos.
El cliente no guarda lógica de seguridad.

✔ Principios REST seguros
	•	Stateless
	•	Verificación cripto del JWT
	•	Validación de issuer y audience

✔ Minimización de privilegios

Solo usuarios con un JWT válido pueden acceder.
No se exponen endpoints innecesarios.

✔ Seguridad basada en el servidor

Todo lo crítico está en el backend: validación, reglas de autorización y políticas.

## 4. Codigo Explicacion

### 1. Clase MessageController

Este controlador de Spring Boot expone una API REST en la ruta `/api` que permite a los clientes enviar y consultar mensajes: cuando se recibe un mensaje por
**POST `/api/messages`**, se valida que no esté vacío, se captura la IP del cliente (considerando proxies), se añade un sello de tiempo del servidor y se guarda en 
el servicio, devolviendo un estado **201 Created**; mientras que al consultar con **GET `/api/messages`**, se retornan los últimos mensajes ya sea en formato JSON 
(lista de objetos con texto, IP y hora) o en texto plano, según el parámetro `format` o el encabezado `Accept`, lo que facilita tanto el almacenamiento como la 
visualización flexible de los mensajes registrados.

### 2. Clase MessageEntry

La clase `MessageEntry` representa un modelo de datos en Java que encapsula un mensaje enviado por el usuario junto con su dirección IP y la marca de tiempo en que 
fue recibido. Se utiliza típicamente en aplicaciones Spring Boot para mapear el cuerpo de una solicitud HTTP (por ejemplo, en un `POST /api/messages`) a un objeto Java 
mediante deserialización automática. Incluye un constructor vacío para compatibilidad con frameworks como Jackson, otro constructor completo para inicialización directa,
y métodos getter/setter para acceder y modificar sus tres atributos: `message`, `clientIp` y `timestamp`. Este modelo facilita el procesamiento estructurado de datos enviados 
desde el frontend.

### 3. Clase AudienceValidator
La clase `AudienceValidator` implementa una validación personalizada para tokens JWT en una aplicación Spring Security, asegurándose de que el token contenga una audiencia
(`aud`) específica. Al recibir un JWT, el método `validate()` verifica si el campo `audience` está presente y si incluye el valor esperado configurado en la instancia; si 
no lo tiene o no coincide, se rechaza el token con un error `invalid_token`. Esta validación es clave para garantizar que el JWT fue emitido para la API correcta y no para 
otro recurso, fortaleciendo la seguridad del backend como Resource Server.

### 4. Clase SecurityConfig
La clase `SecurityConfig` configura la seguridad de una aplicación Spring Boot para proteger rutas y validar tokens JWT emitidos por Auth0. Define un `SecurityFilterChain` 
que desactiva CSRF, permite acceso libre a recursos estáticos, exige autenticación con JWT para rutas `/api/**`, y habilita login web con OAuth2. Además, personaliza la 
conversión de JWT en autoridades (`GrantedAuthority`) mediante el método `extractPermissions`, que transforma los permisos declarados en el token (claim `"permissions"`) 
en roles prefijados como `"PERM_mensaje:leer"`, permitiendo aplicar reglas de autorización más finas en los controladores o servicios.

### 5. Clase MessageService

La clase `MessageService` actúa como un servicio en Spring Boot para gestionar mensajes recibidos, almacenándolos en una lista enlazada con un límite máximo de 100 elementos.
Cada vez que se agrega un nuevo `MessageEntry`, si se supera ese límite se elimina el más antiguo. Además, ofrece métodos sincronizados para recuperar los últimos 10 mensajes 
en forma de lista o como texto plano concatenado, incluyendo el contenido del mensaje, la IP del cliente y la marca de tiempo, lo que facilita tanto el acceso estructurado como 
la visualización rápida de los datos almacenados.


### 5. application.properties
Este archivo de configuración (`application.properties`) define cómo se comporta la aplicación Spring Boot en cuanto a red, seguridad y autenticación con **Auth0**: establece 
que el servidor correrá en el puerto 8080, activa un “toggle” de seguridad (`security.enabled=true`), configura el **Resource Server JWT** para validar tokens emitidos por el
tenant de Auth0 (con `issuer-uri` y `audience`), habilita logging detallado de seguridad para depuración, y finalmente registra los parámetros de **OAuth2 login** con Auth0 
(client-id, client-secret, scopes, tipo de grant, redirect-uri, issuer y token-uri). En conjunto, esto permite que tu aplicación acepte tokens JWT para proteger la API y soporte
inicio de sesión vía Auth0 en el navegador, integrando autenticación centralizada y segura.

## 5. Pruebas realizadas y evidencias del desarrollo


### 1. Sin seguridad

-- Envio de mensajes por medio del cliente html+JS
<img width="1213" height="337" alt="Captura de Pantalla 2025-11-20 a la(s) 8 10 39 p m" src="https://github.com/user-attachments/assets/df84bed1-295b-457c-8f50-6545e6b3e5c5" />

-- Recepcion de los mensajes en el servidor Spring
<img width="581" height="374" alt="Captura de Pantalla 2025-11-20 a la(s) 8 11 13 p m" src="https://github.com/user-attachments/assets/7cad6da5-92ef-4c7b-a29a-6ef7902826e8" />

--GET a la API, endpoint api/message
<img width="1208" height="370" alt="Captura de Pantalla 2025-11-20 a la(s) 8 13 07 p m" src="https://github.com/user-attachments/assets/46df3ee8-f5b5-4ed0-9f7d-583af4c9236d" />

-- Creacion de la API en AUTH0
<img width="840" height="521" alt="Captura de Pantalla 2025-11-20 a la(s) 8 14 08 p m" src="https://github.com/user-attachments/assets/69c90a2c-524c-4895-91a5-ba689454e3ef" />

-- Creacion de la aplicacion TEST asociada a la API en AUTH0
<img width="835" height="519" alt="Captura de Pantalla 2025-11-20 a la(s) 8 14 48 p m" src="https://github.com/user-attachments/assets/0badd3e7-56cc-4a52-8947-59ed453cb57b" />

-- token de la API
<img width="844" height="528" alt="Captura de Pantalla 2025-11-20 a la(s) 8 16 02 p m" src="https://github.com/user-attachments/assets/406935d6-c866-4db6-b035-edbaad37513e" />

-- Obtencion de parametros del token con JWT
<img width="849" height="530" alt="Captura de Pantalla 2025-11-20 a la(s) 8 16 47 p m" src="https://github.com/user-attachments/assets/b64556af-2b53-4110-83c8-0cf2951eb384" />

--Pruebas con seguridad, error 401, porque no esta autorizado, token no puesto
<img width="846" height="317" alt="Captura de Pantalla 2025-11-20 a la(s) 8 18 00 p m" src="https://github.com/user-attachments/assets/4de1d192-022f-4a7c-aaa6-6331b63f4547" />

--Pruebas por consola de la validacion del token, devuelve un json vacio, funciona
<img width="1025" height="334" alt="Captura de Pantalla 2025-11-20 a la(s) 8 18 46 p m" src="https://github.com/user-attachments/assets/e1ce3805-c879-4ca0-869d-cf717ac52719" />

--Pruebas con el cliente, ingreso del token y envio de mensajes
<img width="960" height="378" alt="Captura de Pantalla 2025-11-20 a la(s) 8 19 50 p m" src="https://github.com/user-attachments/assets/6c83ff26-37d2-49d9-ad76-75d7c8a8036d" />

-- Pruebas por consola de la validacion del token, devuelve los mensajes escritos por cliente
<img width="966" height="320" alt="Captura de Pantalla 2025-11-20 a la(s) 8 20 46 p m" src="https://github.com/user-attachments/assets/8b8f3d58-3793-41b7-a3ce-220ab31c1fc8" />

-- Pruebas del almacenamiento de los 10 mensajes (mejora**)
<img width="963" height="450" alt="Captura de Pantalla 2025-11-20 a la(s) 8 21 34 p m" src="https://github.com/user-attachments/assets/4f4a475c-4509-4de2-843d-4ad14e9c66b6" />
<img width="959" height="368" alt="Captura de Pantalla 2025-11-20 a la(s) 8 22 03 p m" src="https://github.com/user-attachments/assets/2bd91aa0-25c3-4b4e-97fd-fff2e18a0a60" />

-- Creacion de la app web para login con credenciales
<img width="959" height="549" alt="Captura de Pantalla 2025-11-20 a la(s) 8 22 37 p m" src="https://github.com/user-attachments/assets/8fba757f-2c79-4cd4-a9f7-ac6e6c9291e7" />

-- Login exitoso
<img width="241" height="367" alt="Captura de Pantalla 2025-11-20 a la(s) 8 23 19 p m" src="https://github.com/user-attachments/assets/a28522d2-a93f-4362-814e-579e811da84f" />
<img width="752" height="488" alt="Captura de Pantalla 2025-11-20 a la(s) 8 23 37 p m" src="https://github.com/user-attachments/assets/8a4e2478-9841-4698-b2fb-1791659c4f1b" />
