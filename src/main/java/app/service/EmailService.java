package app.service;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;

public class EmailService {

    private final static String API_KEY = System.getenv("SENDGRID_API_KEY");

    public boolean sendMailOffer(String name, String email, int totalPrice) throws IOException {
        Email from = new Email("Johannes@johannesfoog.dk");
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


    public boolean sendMailPayment(String name, String email, int totalPrice, int orderNumber) throws IOException {
        Email from = new Email("Johannes@johannesfoog.dk");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("orderNumber", orderNumber);
        personalization.addDynamicTemplateData("price", totalPrice);
        personalization.addDynamicTemplateData("paymentSite", "https://carportfog.showmecode.dk/payment");
        mail.addPersonalization(personalization);
        mail.setTemplateId("d-6a883a6128f542d58457b712c21853df");
        System.out.println("🔔 METODEN BLEV KALDT!");
        System.out.println("Order ID modtaget fra path: " + orderNumber);

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

    public boolean sendMailConfirmation(String name, String email, int totalPrice) throws IOException {
        Email from = new Email("Johannes@johannesfoog.dk");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("email", email);
        personalization.addDynamicTemplateData("price", totalPrice);
        personalization.addDynamicTemplateData("paymentSite", "https://carportfog.showmecode.dk/payment");
        mail.addPersonalization(personalization);
        mail.setTemplateId("d-9ca13dba9799482ca8a989a26e4f92d8");

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



}
