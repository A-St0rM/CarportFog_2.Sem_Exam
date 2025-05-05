package app.service;

import app.controllers.RoutingController;
import app.config.SessionConfig;
import app.config.ThymeleafConfig;

import app.persistence.ConnectionPool;

import com.sendgrid.helpers.mail.objects.Personalization;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.io.IOException;

import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

public class EmailService {

    private Order order;


    public void instantiateEmail() {
    }


    static String API_KEY = System.getenv("SENDGRID_API_KEY");


    public Personalization makePersonalization(Order order) {
        return new Personalization();
    }

    public void sendOffer(Order order) throws IOException {


        Email from = new Email("Johannes@johannesfoog.dk");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(order.getEmail()));
        personalization.addDynamicTemplateData("name", order.getName());
        personalization.addDynamicTemplateData("email", order.getEmail());
        personalization.addDynamicTemplateData("price", order.getPrice());
        mail.addPersonalization(personalization);

        mail.addCategory("carportapp");
        Personalization newPersonalization = makePersonalization(order);
        instantiateEmail();
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
        Email from = new Email("Johannes@johannesfoog.dk");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(order.getEmail()));
        personalization.addDynamicTemplateData("name", order.getName());
        personalization.addDynamicTemplateData("email", order.getEmail());
        personalization.addDynamicTemplateData("price", order.getPrice());
        mail.addPersonalization(personalization);

        mail.addCategory("carportapp");
        Personalization newPersonalization = makePersonalization(order);
        instantiateEmail();
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
