package com.carabuena.it342midterm.controller;

import com.carabuena.it342midterm.dto.ContactForm;
import com.carabuena.it342midterm.service.GoogleContactsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private static final Logger logger = LoggerFactory.getLogger(ContactsController.class);

    @Autowired
    private GoogleContactsService googleContactsService;

    // Show all contacts
    @GetMapping("/all")
    public String showAllContacts(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client,
                                  Model model) throws IOException {
        List<Person> contacts = googleContactsService.getContacts(client);

        // Convert the first contact to JSON and log it
        if (!contacts.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            String contactJson = objectMapper.writeValueAsString(contacts.get(0));
            logger.info("First contact JSON: {}", contactJson);
        }

        model.addAttribute("contacts", contacts);
        return "displayAll";
    }

    // Manage contacts
    @GetMapping("/manage")
    public String manageContacts(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client,
                                 Model model) throws IOException {
        List<Person> contacts = googleContactsService.getContacts(client);
        model.addAttribute("contacts", contacts);
        return "manageContacts";
    }

    // Show form to add a new contact
    @GetMapping("/add")
    public String showAddContactForm(Model model) {
        model.addAttribute("contactForm", new ContactForm());
        return "addContact";
    }

    // Add a new contact
    @PostMapping("/add")
    public String addContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client,
                             @ModelAttribute ContactForm contactForm,
                             RedirectAttributes redirectAttributes) throws IOException {
        try {
            Person newContact = new Person();

            // Set name properly
            List<Name> names = new ArrayList<>();
            Name name = new Name()
                    .setGivenName(contactForm.getGivenName())
                    .setFamilyName(contactForm.getFamilyName());
            names.add(name);
            newContact.setNames(names);

            // Set email
            EmailAddress email = new EmailAddress().setValue(contactForm.getEmail());
            newContact.setEmailAddresses(Collections.singletonList(email));

            // Set phone
            PhoneNumber phone = new PhoneNumber().setValue(contactForm.getPhone());
            newContact.setPhoneNumbers(Collections.singletonList(phone));

            // Create the contact using Google API service
            Person createdContact = googleContactsService.createContact(client, newContact);
            logger.info("Contact added successfully: {}", contactForm.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Contact added successfully!");
        } catch (Exception e) {
            logger.error("Error adding contact: {}", contactForm.getName(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add contact: " + e.getMessage());
        }
        return "redirect:/contacts/manage";
    }

    // Show form to edit an existing contact
    @GetMapping("/edit")
    public String showEditForm(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client,
                               @RequestParam String resourceName,
                               Model model) throws IOException {
        try {
            // Fetch the contact details from Google API
            Person contactToEdit = googleContactsService.getContact(client, resourceName);
            if (contactToEdit != null) {
                ContactForm contactForm = new ContactForm();
                contactForm.setResourceName(resourceName);

                // Set existing contact details with proper name handling
                if (contactToEdit.getNames() != null && !contactToEdit.getNames().isEmpty()) {
                    contactForm.setGivenName(contactToEdit.getNames().get(0).getGivenName());
                    contactForm.setFamilyName(contactToEdit.getNames().get(0).getFamilyName());
                }

                if (contactToEdit.getEmailAddresses() != null && !contactToEdit.getEmailAddresses().isEmpty()) {
                    contactForm.setEmail(contactToEdit.getEmailAddresses().get(0).getValue());
                }

                if (contactToEdit.getPhoneNumbers() != null && !contactToEdit.getPhoneNumbers().isEmpty()) {
                    contactForm.setPhone(contactToEdit.getPhoneNumbers().get(0).getValue());
                }

                model.addAttribute("contactForm", contactForm);
                return "editContact";
            } else {
                logger.warn("Contact not found for editing: {}", resourceName);
                return "redirect:/contacts/manage";
            }
        } catch (Exception e) {
            logger.error("Error fetching contact for editing: {}", resourceName, e);
            return "redirect:/contacts/manage";
        }
    }

    // Update an existing contact
    @PostMapping("/update")
    public String updateContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client,
                                @RequestParam String resourceName,
                                @ModelAttribute ContactForm contactForm,
                                RedirectAttributes redirectAttributes) throws IOException {
        logger.info("Updating contact with resourceName: {}", resourceName);

        try {
            // Fetch existing contact to update
            Person contactToUpdate = googleContactsService.getContact(client, resourceName);

            // Update the fields
            List<Name> names = new ArrayList<>();
            Name name = new Name()
                    .setGivenName(contactForm.getGivenName())
                    .setFamilyName(contactForm.getFamilyName());
            names.add(name);
            contactToUpdate.setNames(names);

            EmailAddress email = new EmailAddress().setValue(contactForm.getEmail());
            contactToUpdate.setEmailAddresses(Collections.singletonList(email));

            PhoneNumber phone = new PhoneNumber().setValue(contactForm.getPhone());
            contactToUpdate.setPhoneNumbers(Collections.singletonList(phone));

            // Update the contact using Google API service
            googleContactsService.updateContact(client, resourceName, contactToUpdate);
            logger.info("Contact updated successfully: {}", contactForm.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Contact updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating contact: {} - {}", resourceName, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update contact: " + e.getMessage());
        }
        return "redirect:/contacts/manage";
    }

    // Delete a contact
    @PostMapping("/delete")
    public String deleteContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client,
                                @RequestParam String resourceName,
                                RedirectAttributes redirectAttributes) {
        try {
            // Delete the contact using Google API service
            googleContactsService.deleteContact(client, resourceName);
            logger.info("Contact deleted successfully: {}", resourceName);
            redirectAttributes.addFlashAttribute("successMessage", "Contact deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete contact: {}", resourceName, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete contact: " + e.getMessage());
        }
        return "redirect:/contacts/manage";
    }
}