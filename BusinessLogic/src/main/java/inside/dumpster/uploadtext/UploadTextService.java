/*
 * 
 */
package inside.dumpster.uploadtext;

import inside.dumpster.backend.repository.StoredData;
import inside.dumpster.backend.repository.TextRepository;
import inside.dumpster.backend.repository.data.Text;
import inside.dumpster.bl.BusinessLogicException;
import inside.dumpster.bl.BusinessLogicService;
import inside.dumpster.monitoring.event.DataUpload;
import static java.awt.SystemColor.text;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.lang3.CharSetUtils.count;

/**
 *
 * @author Joakim Nordstrom joakim.nordstrom@oracle.com
 */
public class UploadTextService extends BusinessLogicService<UploadTextPayload, UploadTextResult>  {

  public UploadTextService(Class<UploadTextPayload> payloadClass, Class<UploadTextResult> resultClass) {
    super(payloadClass, resultClass);
  }
  
  
  @Override
  public UploadTextResult invoke(UploadTextPayload payload) throws BusinessLogicException {

    final DataUpload uploadEvent = new DataUpload();
    uploadEvent.transactionId = payload.getTransactionId();
    uploadEvent.datatype = "Text";
    uploadEvent.srcDevice = payload.getSrcDevice();
  
    final UploadTextResult res = new UploadTextResult();
    
    try {
      uploadEvent.begin();
      
      final InputStream input = payload.getInputStream();
      
      if(input != null) {
        TextRepository repo = new TextRepository();
        Text textData = new Text(input);
        StoredData data = repo.storeData(textData);
        uploadEvent.size = data.getLength();
        res.setResult(String.valueOf(data.getLength()));
      } else {
        uploadEvent.size = -1;
        res.setResult("nodata");
      }
      
    } catch (IOException ex) {
      ex.printStackTrace();
      res.setResult(ex.getMessage());
      throw new BusinessLogicException(ex);
    }
    uploadEvent.end();
    uploadEvent.commit();
    return res;
  }
}
