package six.six.gateway.lyrasms;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


/**
 * LyraSMS service description
 */

@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.TEXT_HTML})
public interface LyraSMSRestService {
    @GET
    @Path("/bulksms/sendSmsAction.do")
    String send(
            @QueryParam("login") String login,
            @QueryParam("password") String password,
            @QueryParam("da") String phonenumber,
            @QueryParam("ud") String message,
            @QueryParam("option") String option,
            @QueryParam("deadline") String deadline,
            @QueryParam("smsHttpId") String smsHttpId,
            @QueryParam("action") String add,
            @QueryParam("forward") String forward,
            @QueryParam("dateEnvoi") String dateEnvoi,
            @QueryParam("vadDomaine") String vadDomaine
    );
}

