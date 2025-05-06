package app.service;

import app.entities.Order;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;


public class EmailService {

    static String API_KEY = System.getenv("SENDGRID_API_KEY");
    private Email from = new Email("Johannes@johannesfoog.dk");


    public void instantiateEmail() {
    }

    public Personalization makePersonalization(Order order) {
        return new Personalization();
    }

    public void sendOffer(Order order) throws IOException {

        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(order.getCustomer().getEmail()));
        //personalization.addDynamicTemplateData("name", order.getName());
        personalization.addDynamicTemplateData("email", order.getCustomer().getEmail());
        personalization.addDynamicTemplateData("price", order.getTotalPrice());
        mail.addPersonalization(personalization);

        mail.addCategory("carportapp");

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            mail.templateId = "d-98633b54660e4e6c839007bc756debd9";
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            System.out.println("Error sending mail");
            throw ex;
        }
    }

    public void sendConfirmation(Order order) throws IOException {
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(order.getCustomer().getEmail()));
        //personalization.addDynamicTemplateData("name", order.getName());
        personalization.addDynamicTemplateData("email", order.getCustomer().getEmail());
        personalization.addDynamicTemplateData("price", order.getTotalPrice());
        mail.addPersonalization(personalization);

        mail.addCategory("carportapp");

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            mail.templateId = "d-9ca13dba9799482ca8a989a26e4f92d8";
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            System.out.println("Error sending mail");
            throw ex;
        }
    }
}
