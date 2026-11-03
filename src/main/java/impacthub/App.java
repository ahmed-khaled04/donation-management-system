package impacthub;

import impacthub.beneficiary.BeneficiaryView;
import impacthub.beneficiary.DistributionService;
import impacthub.beneficiary.Need;
import impacthub.beneficiary.Resource;
import impacthub.donation.DonationService;
import impacthub.donation.DonationView;
import impacthub.event.Event;
import impacthub.event.EventService;
import impacthub.event.EventView;
import impacthub.lookup.LookupView;
import impacthub.lookup.ResourceCategory;
import impacthub.notification.Channel;
import impacthub.notification.NotificationService;
import impacthub.notification.channels.EmailStrategy;
import impacthub.notification.channels.SmsStrategy;
import impacthub.notification.channels.SocialMediaStrategy;
import impacthub.observer.AttendeeNotificationObserver;
import impacthub.observer.ReceiptObserver;
import impacthub.observer.StatsObserver;
import impacthub.observer.StockObserver;
import impacthub.observer.ThankYouObserver;
import impacthub.people.Beneficiary;
import impacthub.people.Donor;
import impacthub.people.PersonService;
import impacthub.people.PersonView;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;
import impacthub.volunteer.VolunteerService;
import impacthub.volunteer.VolunteerView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;

/** Starts the app: wires the services, observers and strategies, then shows the main menu. */
public class App extends Application {
    @Override
    public void start(Stage stage) {
        NotificationService notificationService = new NotificationService();
        notificationService.registerStrategy(Channel.EMAIL, new EmailStrategy());
        notificationService.registerStrategy(Channel.SMS, new SmsStrategy());
        notificationService.registerStrategy(Channel.SOCIAL, new SocialMediaStrategy());
        notificationService.setStrategy(new EmailStrategy());

        PersonService personService = new PersonService();
        DonationService donationService = new DonationService();
        donationService.addObserver(new StatsObserver());
        donationService.addObserver(new ThankYouObserver(notificationService));
        donationService.addObserver(new ReceiptObserver());
        EventService eventService = new EventService();
        eventService.addObserver(new AttendeeNotificationObserver(notificationService));
        DistributionService distributionService = new DistributionService();
        distributionService.addObserver(new StockObserver());
        VolunteerService volunteerService = new VolunteerService();

        if (isEmptyDatabase(personService)) {
            addSampleData(personService, eventService);
        }

        Button people = new Button("People (sign up / log in)");
        people.setOnAction(e -> new PersonView(personService).show());
        Button donations = new Button("Donations");
        donations.setOnAction(e -> new DonationView(donationService, personService).show());
        Button events = new Button("Events");
        events.setOnAction(e -> new EventView(eventService, personService).show());
        Button volunteers = new Button("Volunteers");
        volunteers.setOnAction(e -> new VolunteerView(volunteerService, personService).show());
        Button beneficiaries = new Button("Beneficiaries");
        beneficiaries.setOnAction(e -> new BeneficiaryView(distributionService, personService).show());
        Button lookups = new Button("Lookup tables");
        lookups.setOnAction(e -> new LookupView().show());

        VBox root = new VBox(10, new Label("Impact Hub - NGO Management System"),
                people, donations, events, volunteers, beneficiaries, lookups,
                new Label("Notifications and receipts are printed in the terminal."));
        root.setStyle("-fx-padding: 20;");
        root.getChildren().stream().filter(n -> n instanceof Button).forEach(b -> ((Button) b).setMaxWidth(260));

        stage.setTitle("Impact Hub");
        stage.setScene(new Scene(root, 380, 360));
        stage.show();
    }

    /** Deleting every person must not bring the sample data back on top of the other tables. */
    private static boolean isEmptyDatabase(PersonService personService) {
        return personService.searchByName("").isEmpty()
                && new GenericDAO<>(Skill.class).findAll().isEmpty()
                && new GenericDAO<>(ResourceCategory.class).findAll().isEmpty()
                && new GenericDAO<>(Resource.class).findAll().isEmpty()
                && new GenericDAO<>(Event.class).findAll().isEmpty();
    }

    /** First run only: a few people, lookup rows, resources and an event to try the app with. */
    private static void addSampleData(PersonService personService, EventService eventService) {
        GenericDAO<Skill> skills = new GenericDAO<>(Skill.class);
        Skill firstAid = new Skill("First aid");
        Skill teaching = new Skill("Teaching");
        skills.insert(firstAid);
        skills.insert(teaching);
        skills.insert(new Skill("Cooking"));

        GenericDAO<ResourceCategory> categories = new GenericDAO<>(ResourceCategory.class);
        ResourceCategory food = new ResourceCategory("Food");
        ResourceCategory education = new ResourceCategory("Education");
        categories.insert(food);
        categories.insert(education);

        GenericDAO<Resource> resources = new GenericDAO<>(Resource.class);
        resources.insert(new Resource("Rice bag", "bags", 200, food));
        resources.insert(new Resource("School kit", "kits", 100, education));

        Donor sara = (Donor) personService.register("Donor", "Sara Ali", "sara@mail.com", "01000000001", "1234");
        sara.setTaxId("TX-1001");
        personService.update(sara);
        Donor omar = (Donor) personService.register("Donor", "Omar Co.", "omar@mail.com", "01000000002", "1234");
        omar.setDonorType("Corporate");
        omar.setPreferredChannel(Channel.SMS);
        personService.update(omar);

        Volunteer ali = (Volunteer) personService.register("Volunteer", "Ali Hassan", "ali@mail.com", "01000000003", "1234");
        ali.getSkills().add(firstAid);
        ali.getAvailability().add("Weekends");
        ali.getPreferences().add("Outreach");
        personService.update(ali);
        Volunteer mona = (Volunteer) personService.register("Volunteer", "Mona Adel", "mona@mail.com", "01000000004", "1234");
        mona.getSkills().add(teaching);
        mona.getAvailability().add("Evenings");
        mona.getPreferences().add("Workshop");
        mona.setPreferredChannel(Channel.SOCIAL);
        mona.setSocialHandle("mona.adel");
        personService.update(mona);

        Beneficiary fatma = (Beneficiary) personService.register("Beneficiary", "Fatma Said", "fatma@mail.com", "01000000005", "1234");
        fatma.setAddress("Cairo");
        fatma.setFamilySize(5);
        fatma.getNeeds().add(new Need(food, "Monthly food basket", 1));
        personService.update(fatma);
        Beneficiary hassan = (Beneficiary) personService.register("Beneficiary", "Hassan Omar", "hassan@mail.com", "01000000006", "1234");
        hassan.setAddress("Giza");
        hassan.setFamilySize(3);
        hassan.getNeeds().add(new Need(education, "School supplies for 2 children", 2));
        personService.update(hassan);

        eventService.createEvent("Fundraiser", "Charity Gala", LocalDateTime.now().plusDays(14).withHour(19).withMinute(0), "Grand Hall");
        eventService.createEvent("Workshop", "First Aid Training", LocalDateTime.now().plusDays(7).withHour(10).withMinute(0), "Community Center");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
