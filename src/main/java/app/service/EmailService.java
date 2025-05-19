package app.service;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;

public class EmailService {

    private final static String API_KEY = System.getenv("SENDGRID_API_KEY");

    public boolean sendMailOffer(String name, String email, int totalPrice) throws IOException {
        Email from = new Email("johannesfoog@gmail.com");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("email", email);
        personalization.addDynamicTemplateData("price", totalPrice);
        mail.addPersonalization(personalization);
        mail.setTemplateId("d-98633b54660e4e6c839007bc756debd9");

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("SendGrid Offer Response Status Code: " + response.getStatusCode());
            return true;

        } catch (IOException ex) {
            System.err.println("Fejl ved afsendelse af tilbudsmail: " + ex.getMessage());
            throw ex; // sender fejlen videre
        }
    }

    public boolean sendMailPayment(String name, String email, int orderNumber) {

        Email from = new Email("johannesfoog@gmail.com");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("orderNumber", orderNumber);
        String paymentLink = "https://carportfog.showmecode.dk/payment?orderId=" + orderNumber;
        personalization.addDynamicTemplateData("paymentSite", paymentLink);

        mail.addPersonalization(personalization);
        mail.setTemplateId("d-6a883a6128f542d58457b712c21853df");

        try {
            SendGrid sg = new SendGrid(API_KEY);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("SendGrid Offer Response Status Code: " + response.getStatusCode());
            return true;

        } catch (Exception ex) {
            System.err.println("FEJL under afsendelse: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean sendMailConfirmation(String name, String email, int totalPrice, List<Map<String, Object>> bomItems) throws IOException {
        Email from = new Email("johannesfoog@gmail.com");
        from.setName("Johannes Fog Byggemarked");


        Mail mail = new Mail();
        mail.setFrom(from);


        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("email", email);
        personalization.addDynamicTemplateData("price", totalPrice);
        personalization.addDynamicTemplateData("bom_items", bomItems);
        mail.addPersonalization(personalization);
        mail.setTemplateId("d-9ca13dba9799482ca8a989a26e4f92d8");


        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            return true;
        } catch (IOException ex) {
            System.err.println("Fejl ved afsendelse af bekræftelsesmail: " + ex.getMessage());
            throw ex;
        }
    }


}
