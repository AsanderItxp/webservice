package ru.asander.ws.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;
import com.sun.xml.ws.encoding.DataSourceStreamingDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Map;
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
 */
public class StreamingClientRunner {
    private static final Logger LOG = LoggerFactory.getLogger(StreamingClientRunner.class);

    private final static QName STREAM_FILE_SERVICE_QNAME = new QName("http://www.asander.ru/ws/service/", "StreamFileService");

    //Use big file for transfer through stream.   Here is an example.
    private final static URL SOURCE_FILE_URL = StreamingClientRunner.class.getClassLoader().getResource("data/source_file_book.pdf");
    private final static String DESTINATION_FILE_PATH = String.format("dest_file_%s.bin", UUID.randomUUID().toString());

    public static void main(String[] args) throws IOException {
        Args cmd = new Args();
        JCommander commander = new JCommander();
        commander.addObject(cmd);
        commander.parse(args);

        LOG.info("\n ===============================");
        //create service endpoint
        URL wsdlLocation = new URL(cmd.endpoint);
        LOG.debug("Creating service for endpoint: {} and chunkSize: {}", cmd.endpoint, cmd.chunkSize);
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

        LOG.debug("Service created!");
        StreamingDataHandler dh = null;
        //Create DataHandler for file
        try {

//            dh = new DataSourceStreamingDataHandler(new FileDataSource(new File("U:\\source_file.bin"))); //TODO NPE
            dh = new DataSourceStreamingDataHandler(new FileDataSource(SOURCE_FILE_URL.getFile())); //TODO NPE
            UploadDocReqType docReqType = new UploadDocReqType();
            DocumentType dt = new DocumentType();
            dt.setDocName(DESTINATION_FILE_PATH);
            dt.setData(dh);
            docReqType.setDocument(dt);

            //Call web service
            LOG.info("Call uploadDoc web-method. Waiting...");
            UploadDocRespType response = port.uploadDoc(docReqType);

            if (response != null && response.getResultData() != null && "Ok".equals(response.getResultData().getResultCode())) {
                LOG.info("Upload doc executed SUCCESS and saved to {} on server! ", DESTINATION_FILE_PATH);
            }
            LOG.info("Finished.");
        } catch (Exception e) {
            LOG.error("Error executed service:  Details: {}", e);
        }

        LOG.info("\n ===============================");
    }


    private static final class Args {

        @Parameter(required = true, names = {"-e", "--endpoint"}, description = "the web-service endpoint")
        private String endpoint = "http://localhost:8080/test/StreamFileService?wsdl";

        @Parameter(names = {"-cs", "--chunkSize"}, description = "The chunk size in bytes")
        private Integer chunkSize = 8192;

    }
}
