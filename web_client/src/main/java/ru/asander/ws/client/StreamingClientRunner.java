package ru.asander.ws.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;
import com.sun.xml.ws.encoding.DataSourceStreamingDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.asander.ws.common.result.IntegrationSimpleResultDataType;
import ru.asander.ws.service.StreamService;
import ru.asander.ws.uploaddoc.req.DocumentType;
import ru.asander.ws.uploaddoc.req.UploadDocReqType;
import ru.asander.ws.uploaddoc.resp.UploadDocRespType;

import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ========== StreamingClient.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 11.06.2019 10:05: Original version (AAVolkov)<br/>
 * <p>
 * Пример параметров запуска -e http://localhost:8080/test/StreamFileService?wsdl -fp U:\\source_file.bin
 */
public class StreamingClientRunner {
    private static final Logger log = LoggerFactory.getLogger(StreamingClientRunner.class);

    private final static QName STREAM_FILE_SERVICE_QNAME = new QName("http://www.asander.ru/ws/service/", "StreamFileService");
    private final static String DESTINATION_FILE_TEMPLATE = "dest_file_%s.bin";

    private StreamingClientRunner() {
        throw new IllegalAccessError("Utility class");
    }

    public static void main(String[] args) throws IOException {
        Args cmd = new Args();
        JCommander.newBuilder()
                .addObject(cmd)
                .build()
                .parse(args);


        log.info("\n ===============================");
        //create service endpoint
        URL wsdlLocation = new URL(cmd.endpoint);
        log.debug("Creating service for endpoint: {} and chunkSize: {}", cmd.endpoint, cmd.chunkSize);

        //StreamingAttachmentFeature saf = new StreamingAttachmentFeature("/tmp", true, 4000000L);

        //Creating web-service client
        Service service = Service.create(wsdlLocation, STREAM_FILE_SERVICE_QNAME);
        // Getting service port via Service Interface.
        StreamService port = service.getPort(StreamService.class);
        //StreamService port = service.getPort(StreamService.class, new MTOMFeature());


        if (cmd.chunkSize > 0) {
            //Если не указать CHUNK_SIZE  - то все пойдет в память.
            Map<String, Object> ctxt = ((BindingProvider) port).getRequestContext();
            ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, cmd.chunkSize);
        }

        log.debug("Service created!");
        //Create DataHandler for file
        UploadDocReqType docReqType = new UploadDocReqType();
        List<DocumentType> dTypes = docReqType.getDocuments();
        //Просто передача списка документов
        for (int i = 0; i < 1; i++) {
            DocumentType dt = fillDocument(cmd.getSourceFile(), String.valueOf(i));
            dTypes.add(dt);
        }

        //Call web service
        log.info("Call uploadDoc web-method. Waiting...");
        UploadDocRespType response = port.uploadDoc(docReqType);

        if (Optional.of(response)
                .map(UploadDocRespType::getResultData)
                .map(IntegrationSimpleResultDataType::getResultCode).filter("Ok"::equals).isPresent()) {
            log.info("Upload doc executed SUCCESS and saved to {} on server! ", response.getDocument());
        }
        log.info("Finished!");
        log.info("\n ===============================");
    }

    private static DocumentType fillDocument(File sourceFile, String docType) throws IOException {
        try (StreamingDataHandler dh = new DataSourceStreamingDataHandler(
                new FileDataSource(sourceFile))) {
            DocumentType dt = new DocumentType();
            dt.setDocName(String.format(DESTINATION_FILE_TEMPLATE, UUID.randomUUID().toString()));
            dt.setDocType(docType);
            dt.setData(dh);
            return dt;
        }
    }


    private static final class Args {

        @Parameter(required = true, names = {"-e", "--endpoint"}, description = "the web-service endpoint")
        private String endpoint = "http://localhost:8080/test/StreamFileService?wsdl";  //default value

        @Parameter(names = {"-cs", "--chunkSize"}, description = "The chunk size in bytes")
        private Integer chunkSize = 8192;  //default value

        @Parameter(required = true, names = {"-fp", "--filePath"}, description = "Set the source file path.")
        private String sourceFilePath;

        File getSourceFile() {
            return new File(sourceFilePath);
        }
    }
}
