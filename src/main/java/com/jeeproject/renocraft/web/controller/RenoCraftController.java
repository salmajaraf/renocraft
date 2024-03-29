package com.jeeproject.renocraft.web.controller;



import com.jeeproject.renocraft.entity.*;
import com.jeeproject.renocraft.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import com.jeeproject.renocraft.entity.Contact;
import com.jeeproject.renocraft.entity.Employeur;
import com.jeeproject.renocraft.service.ContactService;
import com.jeeproject.renocraft.service.EmployeurService;
import com.jeeproject.renocraft.service.ServiceService;
import jakarta.servlet.http.HttpServletRequest;

import com.jeeproject.renocraft.entity.User;
import com.jeeproject.renocraft.service.UserService;
import com.jeeproject.renocraft.service.CommandePackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class RenoCraftController {
    @Autowired
    private  DashboardService dashboardService;
    @Autowired
    private UserService service;
    @Autowired
    private  CommandePackService commandepackService;
    private final ContactService contactService;
    private final EmployeurService employeurService;
    private final ServiceService serviceService;
    private final UserService userService;
    private final CommandeService commandeService;
    private final PackService packService;
    private Long actualServiceId = 1L;

    public RenoCraftController(ContactService contactService,
                               EmployeurService employeurService,
                               ServiceService serviceService,
                               UserService userService,CommandeService commandeService,PackService packService) {
        this.contactService = contactService;
        this.employeurService = employeurService;
        this.serviceService = serviceService;
        this.userService = userService;
        this.commandeService=commandeService;
        this.packService=packService;
    }


    //mapping
    @GetMapping("/")
    public String welcomePage() {
        return "index";
    }


    @GetMapping("/signup")
    public String register(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "signup";
    }


    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        if (service.usernameExists(user.getUsername())) {
            model.addAttribute("usernameExists", true);
            return "signup";
        }
        service.registerUser(user);
        return "signin";
    }

    @GetMapping("/success")
    public String successPage() {
        return "success";
    }

    @GetMapping("/packs")
    public String packsPage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {
            return "pack";
        } else {
            return "redirect:/signin";
        }
    }


        @GetMapping ("/cart")
        public String cartPage(HttpServletRequest request, Model model){
            HttpSession session = request.getSession();
            if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {
                String user = (String) session.getAttribute("userName");
                Optional<User> cart = userService.getUser(user);
                model.addAttribute("cart", cart);
                return "cart";
            } else {
                return "redirect:/signin";
            }
        }
    @GetMapping ("/CollectionsProduits")
    public String CollectionsPage(HttpServletRequest request){
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {
            return "CollectionsProduits";
        } else {
            return "redirect:/signin";
        }
    }

    @PostMapping ("/cart")
    public String processCommande(@RequestParam("dateCommande") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateCommande,
                                  @RequestParam("heureCommande") @DateTimeFormat(pattern = "HH:mm") Date heureCommande,
                                  @RequestParam("packIds") List<Long> packIds,
                                  @SessionAttribute("userName") String username){
        Optional<User> optionalUser = userService.getUser(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();


            Commande commande = new Commande();
            commande.setUser(user);
            commande.setDateCommande(dateCommande);
            commande.setHeureCommande(heureCommande);


            List<Pack> packs = packService.findPacksByIds(packIds);
            commande.setPacks(packs);


            commandeService.addCommande(commande);


            return "redirect:/cart";
        }
        else{
            return "redirect:/error";
        }
    }






    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/signin";
    }





    //controle service
    @GetMapping("/contact")
    public String showContactForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "fragments/contact";
    }

    @PostMapping("/contact")
    public String processContactForm(@ModelAttribute("contact") Contact contact) {
        contactService.addContact(contact);
        return "redirect:/success";
    }

    @GetMapping("/service")
    public String getEmployeesByService(HttpServletRequest request,
                                        @RequestParam(required = false) Long serviceId,
                                        @RequestParam(required = false) String city,
                                        Model model) {
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {
            actualServiceId = (serviceId != null) ? serviceId : actualServiceId;
            List<Employeur> employeurs;

            if (city == null) {
                employeurs = employeurService.getEmployeurByService(actualServiceId);
            } else {
                employeurs = employeurService.getEmployeurByServiceCity(actualServiceId, city);
            }
            model.addAttribute("employeurs", employeurs);
            model.addAttribute("actualServiceId", actualServiceId); // Adding actualServiceId to the model
            return "service";
        } else {
            return "redirect:/signin";
        }
    }

    @GetMapping("/home")
    public String homeAfterConn(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {

            return "home";
        } else {
            return "redirect:/signin";
        }
    }

    @GetMapping("/profil")
    public String getProfil(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {
            String user = (String) session.getAttribute("userName");
            Optional<User> profil = userService.getUser(user);
            model.addAttribute("profil", profil);
            return "profil";
        } else {
            return "redirect:/signin";
        }
    }

    @PostMapping("/profil")
    public String updateFormClient(@RequestParam("name") String name,
                                   @RequestParam("usernamechamp") String usernamechamp,
                                   @RequestParam("email") String email,
                                   @RequestParam("phone") String phone) {
        userService.updateUser(usernamechamp, name, email, phone);
        return "redirect:/home";
    }

    @GetMapping("/signin")
    public ModelAndView signin() {
        ModelAndView mav = new ModelAndView("signin");
        mav.addObject("user", new User());
        return mav;
    }

    @PostMapping("/signin")
    public String signin(@ModelAttribute("user") User user, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User oauthUser = service.signin(user.getUsername(), user.getPassword());
        System.out.println(oauthUser);
        if (session != null && oauthUser != null) {
            session.setAttribute("connexion", true);
            String userName = oauthUser.getUsername();
            session.setAttribute("userName", userName);
            if (userName.equals("admin")) {
                return "redirect:/dashHome";
            } else {
                return "redirect:/home";
            }
        } else {
            return "redirect:/signin-error";
        }
    }


    @GetMapping("/dashHome")
    public String getDashHome(HttpServletRequest request, Model model) {
        String currentMonth = getCurrentMonth();
        String previousMonth = getPreviousMonth();

        long commandesMoisActuel = commandeService.getCommandesByMonth("01");
        long commandesMoisPrecedent = commandeService.getCommandesByMonth("12");
        List<Object[]> statistics = commandepackService.getTopPacksStatistics();
        model.addAttribute("statistics", statistics);

        // Ajoutez les statistiques au modèle pour les rendre disponibles dans la vue
        model.addAttribute("commandesMoisActuel", commandesMoisActuel);
        model.addAttribute("commandesMoisPrecedent", commandesMoisPrecedent);


        model.addAttribute("nbrclients",dashboardService.getNombreClients());
        model.addAttribute("nbrcommandes",dashboardService.getNombreCommandes());
        model.addAttribute("nbremployes",dashboardService.getNombreEmployes());
        model.addAttribute("nbrcontacts",dashboardService.getNombreContacts());
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            return "Dash/dashHome";
        } else {
            return "redirect:/signin";
        }
    }
    private String getCurrentMonth() {
        // Logique pour obtenir le mois actuel (vous pouvez utiliser LocalDate pour obtenir le mois actuel)
        return "2024-01"; // Remplacez cela par la logique réelle
    }

    private String getPreviousMonth() {
        // Logique pour obtenir le mois précédent (vous pouvez utiliser LocalDate pour obtenir le mois précédent)
        return "2023-12"; // Remplacez cela par la logique réelle
    }

    @GetMapping("/dashClient")
    public String getDashClients(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            List<User> clients = userService.getClient();
            model.addAttribute("clients", clients);
            return "Dash/dashClient";
        } else {
            return "redirect:/signin";
        }
    }
    @GetMapping("/updateClient")
    public String getUpClient(HttpServletRequest request, Model model,@RequestParam("userparam") String userparam) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            Optional<User> userModif = userService.getUser(userparam);
            model.addAttribute("userModif", userModif);
            return "Dash/dashClientUpdate";
        } else {
            return "redirect:/signin";
        }
    }

    @GetMapping("/dashPack")
    public String getDashPack(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            List<Pack> packs = packService.selectPacks();
            model.addAttribute("packs", packs);
            return "Dash/dashPack";
        } else {
            return "redirect:/signin";
        }
    }

    @GetMapping("/dashEmployeur")
    public String getDashEmployeur(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            List<Employeur> employeurs = employeurService.getEmployeur();
            model.addAttribute("employeurs", employeurs);
            return "Dash/dashEmp";
        } else {
            return "redirect:/signin";
        }
    }


    @PostMapping("/suppUser")
    public String deleteUserDash(@RequestParam("usernameparam") String usernameparam) {
        userService.deleteClient(usernameparam);
        return "redirect:/dashClient";
    }
    @GetMapping("/dashService")
    public String getDashService(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            List<Service> services = serviceService.getService();
            model.addAttribute("services", services);
            return "Dash/dashService";
        } else {
            return "redirect:/signin";
        }
    }

    @PostMapping("/updateFormUser")
    public String updateFormUserMeth(@RequestParam("name") String name,
                                     @RequestParam("usernamechamp") String usernamechamp,
                                     @RequestParam("email") String email,
                                     @RequestParam("phone") String phone) {
        userService.updateUser(usernamechamp, name, email, phone);
        return "redirect:/dashClient";
    }

    @GetMapping("/dashContact")
    public String getDashContact(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            List<Contact> contacts = contactService.getContact();
            int size = contactService.contactSize();
            model.addAttribute("contacts", contacts);
            model.addAttribute("size", size);
            return "Dash/dashContact";
        } else {
            return "redirect:/signin";
        }
    }

    @GetMapping("/updateService")
    public String getUpService(HttpServletRequest request, Model model,@RequestParam("userparam") Long id) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            Optional<Service> serviceModif = serviceService.getServiceById(id);
            model.addAttribute("serviceModif", serviceModif);
            return "Dash/dashServiceUpdate";
        } else {
            return "redirect:/signin";
        }
    }
    @PostMapping("/updateFormService")
    public String updateFormServiceMeth(@RequestParam("name") String nom,
                                        @RequestParam("description") String description,
                                        @RequestParam("id_service") Long service_id) {
        serviceService.updateService(service_id, nom, description);
        return "redirect:/dashService";
    }

    @GetMapping("/updateEmployeur")
    public String getUpEmp(HttpServletRequest request, Model model,@RequestParam("userparam") Long id) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            Optional<Employeur> empModif = employeurService.getEmployeurById(id);
            model.addAttribute("empModif", empModif);
            return "Dash/dashEmpUpdate";
        } else {
            return "redirect:/signin";
        }
    }
    @PostMapping("/updateFormEmp")
    public String updateFormEmpMeth(@RequestParam("name") String nom,
                                        @RequestParam("prenom") String prenom,
                                        @RequestParam("id_employeur") Long id_employeur,
                                        @RequestParam("numero") String numero,
                                        @RequestParam("ville") String ville,
                                        @RequestParam("service") Long service) {
        employeurService.updateEmployeur(id_employeur, nom, prenom,numero,ville ,service );
        return "redirect:/dashEmployeur";
    }
    @PostMapping("/suppEmp")
    public String deleteEmpDash(@RequestParam("usernameparam") Long id) {
        employeurService.deleteEmp(id);
        return "redirect:/dashEmployeur";
    }

    @GetMapping("/addEmployeur")
    public String getAddEmp(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("userName");
        model.addAttribute("userNameDash", user);
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")&& user.equals("admin")) {
            return "Dash/dashEmpAdd";
        } else {
            return "redirect:/signin";
        }
    }
    @PostMapping("/addFormEmp")
    public String addFormEmpMeth(@RequestParam("name") String nom,
                                 @RequestParam("prenom") String prenom,
                                 @RequestParam("numero") String numero,
                                 @RequestParam("ville") String ville,
                                 @RequestParam("service") Long id_service) {
        employeurService.addEmployeur(nom, prenom, numero, ville, id_service);
        return "redirect:/dashEmployeur";
    }


    @GetMapping("/updateAdmin")
    public String updateAdminPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("connexion") != null && (boolean) session.getAttribute("connexion")) {
            String user = (String) session.getAttribute("userName");
            Optional<User> updateAdmin = userService.getUser(user);
            model.addAttribute("updateAdmin", updateAdmin);
            model.addAttribute("userNameDash",user);
            return "Dash/dashProfile";
        } else {
            return "redirect:/signin";
        }
    }

    @PostMapping("/updateAdmin")
    public String updateFormAdmin(@RequestParam("name") String name,
                                     @RequestParam("usernamechamp") String usernamechamp,
                                     @RequestParam("email") String email,
                                     @RequestParam("phone") String phone) {
        userService.updateUser(usernamechamp, name, email, phone);
        return "redirect:/dashHome";
    }




}