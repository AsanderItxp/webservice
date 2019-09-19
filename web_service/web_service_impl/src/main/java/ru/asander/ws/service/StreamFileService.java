package ru.asander.ws.service;

import com.sun.xml.ws.developer.StreamingAttachment;
import com.sun.xml.ws.developer.StreamingDataHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.asander.ws.common.result.IntegrationSimpleResultDataType;
import ru.asander.ws.uploaddoc.req.UploadDocReqType;
import ru.asander.ws.uploaddoc.resp.UploadDocRespType;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        parseEagerly = false,      //TRUE - Сперва ждем загрузки файла. FALSE - Наоборот сначала вход в метод потом качаем стрим.
        memoryThreshold = 40000L,  //Размер буфера для хранения в памяти.
        dir = "U:\\temp"        //Временный каталог сохранения данных.
)
@MTOM
public class StreamFileService implements StreamService {
    private static final Logger log = LoggerFactory.getLogger(StreamFileService.class);

    public UploadDocRespType uploadDoc(UploadDocReqType req) {
        log.info("\n =============================== ");
        log.debug("\nin uploadDoc started...");
        StreamingDataHandler sdh = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            DataHandler dh = req.getDocument().getData();

            if (dh instanceof StreamingDataHandler) {
                sdh = (StreamingDataHandler) dh;
                in = sdh.readOnce(); // после прочтения файл автоматически удаляется. Поток более не доступен.
                log.info("StreamingDataHandler: name: {}", sdh.getName());
                log.info("StreamingDataHandler: contentType: {}", sdh.getContentType());
            } else {
                in = dh.getInputStream();
                //throw new UnsupportedDataTypeException("Не поддерживаемый тип данных. Ожидается  StreamingDataHandler получено : " + dh.getClass());
            }

            out = new FileOutputStream(req.getDocument().getDocName());

            log.debug("in uploadDoc finished received");
            IOUtils.copy(in, out);
            log.debug("in uploadDoc file moved to {}", req.getDocument().getDocName());
            log.info("\n===============================");

        } catch (Exception e) {
            throw new WebServiceException(e);
        } finally {
            //Java6
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(sdh);
        }

        UploadDocRespType response = new UploadDocRespType();
        response.setDocID(req.getDocument().getDocName());
        IntegrationSimpleResultDataType rData = new IntegrationSimpleResultDataType();
        rData.setResultCode("Ok");
        response.setResultData(rData);
        return response;
    }


}
