### Пример для реализации сервиса для потоковой передачи файлов большого объема

    Пример иллюстрирует возможность использования InputStream для передачи файлов на сервер в SOAP web-сервисе.
  
  >Проблематика:    Падение серверов по OOM при загрузке файлов большого объема (> 900Мб)

#### Модули:

+  **web-service** 
    + **web-service_api** - описание интерфейса сервиса.
    + **web-service_impl** - реализация  сервиса + сборка  артефакта для запуска.
+ **web-client** - пример клиента для передачи файла.

Объектная модель формируется на основании *.xsd файлов.
Для использования DataHandler достаточно  в *.xsd файле указать атрибут xmime:expectedContentTypes="application/octet-stream" 
 для элемента с типом base64Binary:

``` xml
      <xs:element name="Data" type="xs:base64Binary" minOccurs="1" maxOccurs="1"
          xmime:expectedContentTypes="application/octet-stream">
				<xs:annotation>
					<xs:documentation>Тело документа</xs:documentation>
				</xs:annotation>
			</xs:element>
```
см. файл 
>jaxws_streaming\web_service\web_service_api\src\main\resources\xsd\upload_doc_req.xsd

----
**Запуск сервера:** 
    mvn clean install tomcat7:run
----
**Запуск клиента:** 
    mvn -f web_client/pom.xml clean install exec:java
----

**Ресурсы в помошь:**
> https://docs.oracle.com/middleware/1212/wls/WSGET/jax-ws-mtom.htm#WSGET3469

----