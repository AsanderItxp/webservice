package ru.asander.ws.service;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import com.sun.xml.ws.developer.StreamingAttachment;
import com.sun.xml.ws.developer.StreamingDataHandler;
import io.astefanutti.metrics.aspectj.Metrics;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.asander.ws.common.result.IntegrationSimpleResultDataType;
import ru.asander.ws.uploaddoc.req.DocumentType;
import ru.asander.ws.uploaddoc.req.UploadDocReqType;
import ru.asander.ws.uploaddoc.resp.UploadDocRespType;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * ========== asander.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 07.06.2019 15:14: Original version (AAVolkov)<br/>
 */
@WebService(portName = "StreamFileServicePort",
        serviceName = "StreamFileService",
        targetNamespace = "http://www.asander.ru/ws/service/",
        endpointInterface = "ru.asander.ws.service.StreamService")
@StreamingAttachment(
        parseEagerly = false,                      //TRUE - Сперва ждем загрузки файла. FALSE - Наоборот сначала вход в метод потом качаем стрим.
        memoryThreshold = 40000L,                  //Размер буфера для хранения в памяти.
        dir = "U:\\temp\\StreamFileService"        //Каталог сохранения временных данных.  //todo to config
)
@BindingType(value = SOAPBinding.SOAP12HTTP_MTOM_BINDING)
@MTOM
@Metrics(registry = "default")
public class StreamFileService implements StreamService {
    private static final Logger log = LoggerFactory.getLogger(StreamFileService.class);
    private static final String DESTINATION_DIR = "U:\\StreamFileService\\Result\\";  //todo to config


    private final MetricRegistry smr = SharedMetricRegistries.getOrCreate("default");

    private final Meter requests = smr.meter(MetricRegistry.name(StreamFileService.class, "requests"));
    private final Meter data = smr.meter(MetricRegistry.name(StreamFileService.class, "data"));
    private final Counter counter = smr.counter(MetricRegistry.name(StreamFileService.class, "bytes"));
    private final Timer timer = smr.timer(MetricRegistry.name(StreamFileService.class, "timer"));
    private final Histogram hist = smr.histogram(MetricRegistry.name(StreamFileService.class, "histogram"));

    @PostConstruct
    private void init() {
        log.debug("Initialization service!");
    }


    //@ExceptionMetered(name = "uploadDoc.Exceptions")
    @Timed
    public UploadDocRespType uploadDoc(UploadDocReqType req) {
        requests.mark(); // Счетчик исполнения
        hist.update(1); //Гистограмма внутреннего выполнения метода. Шаг1
        log.debug("\n####### UploadDoc started... #########");

        hist.update(2);  //Гистограмма внутреннего выполнения метода. Шаг2
        UploadDocRespType response = new UploadDocRespType();

        //Если делать разделения на потоки, то скорость приема не зависит от числа потоков.
        for (DocumentType document : req.getDocuments()) {
            log.debug("Prepare doc: {}", document.getDocName());
            String savedDocPath = saveDocument(document);

            ru.asander.ws.uploaddoc.resp.DocumentType respDoc = new ru.asander.ws.uploaddoc.resp.DocumentType();
            respDoc.setDocID(savedDocPath);
            respDoc.setDocURL(savedDocPath);
            response.getDocument().add(respDoc);
        }
        log.debug("Prepare finished!!!!");

        hist.update(3);   //Гистограмма внутреннего выполнения метода. Шаг3

        IntegrationSimpleResultDataType rData = new IntegrationSimpleResultDataType();
        rData.setResultCode("Ok");
        response.setResultData(rData);
        log.info("########## STOP!!! ###########");
        return response;
    }

    @Timed
    public String saveDocument(DocumentType document) {
        //TODO build targetPath. ? From Request ?
        String targetPath = DESTINATION_DIR + String.format(document.getDocName(), UUID.randomUUID());

        try (Timer.Context ignored = timer.time();
             StreamingDataHandler sdh = (StreamingDataHandler) document.getData();
             InputStream in = sdh.readOnce(); // после прочтения файл автоматически удаляется. Поток более не доступен.
             OutputStream out = new FileOutputStream(targetPath)
        ) {
            log.info(">>>> StreamingDataHandler: Start upload fileName:{} docType:{}", document.getDocName(), document.getDocType());
            long numberOfBytes = IOUtils.copyLarge(in, out);  // Собственно вся соль логики.  Копирование потока.

            counter.inc(numberOfBytes);   // Считаем  число байт принятых данных.
            data.mark(numberOfBytes);     // Метрика переданных байт данных.
            log.debug("<<<< StreamingDataHandler:Stop upload fileName:{} saved to {}", document.getDocName(), targetPath);
        } catch (Exception e) {
            log.error("Can't save file :{}. Error details: {}", targetPath, e);
            throw new WebServiceException(e);
        }
        return targetPath;
    }
}
