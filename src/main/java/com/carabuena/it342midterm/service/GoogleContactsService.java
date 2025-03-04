package com.carabuena.it342midterm.service;

                import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
                import com.google.api.client.http.javanet.NetHttpTransport;
                import com.google.api.client.json.jackson2.JacksonFactory;
                import com.google.api.services.people.v1.PeopleService;
                import com.google.api.services.people.v1.model.ListConnectionsResponse;
                import com.google.api.services.people.v1.model.Person;
                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;
                import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
                import org.springframework.stereotype.Service;

                import java.io.IOException;
                import java.util.ArrayList;
                import java.util.List;

                @Service
                public class GoogleContactsService {

                    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);

                    private PeopleService createPeopleService(OAuth2AuthorizedClient authorizedClient) {
                        GoogleCredential credential = new GoogleCredential().setAccessToken(authorizedClient.getAccessToken().getTokenValue());
                        return new PeopleService.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                                .setApplicationName("Carabuena IT342 Midterm")
                                .build();
                    }

                    public Person createContact(OAuth2AuthorizedClient authorizedClient, Person newContact) throws IOException {
                        PeopleService peopleService = createPeopleService(authorizedClient);
                        logger.debug("Creating contact with name: {}", newContact.getNames().get(0).getDisplayName());
                        Person result = peopleService.people().createContact(newContact).execute();
                        logger.info("Contact created: {}", result.getResourceName());
                        return result;
                    }

                    public List<Person> getContacts(OAuth2AuthorizedClient authorizedClient) throws IOException {
                        PeopleService peopleService = createPeopleService(authorizedClient);
                        List<Person> allContacts = new ArrayList<>();
                        String nextPageToken = null;

                        do {
                            ListConnectionsResponse response = peopleService.people().connections()
                                    .list("people/me")
                                    .setPersonFields("names,emailAddresses,phoneNumbers")
                                    .setPageToken(nextPageToken)
                                    .execute();

                            if (response.getConnections() != null) {
                                allContacts.addAll(response.getConnections());
                            }
                            nextPageToken = response.getNextPageToken();
                        } while (nextPageToken != null);

                        return allContacts;
                    }

                    public Person getContact(OAuth2AuthorizedClient client, String resourceName) throws IOException {
                        PeopleService peopleService = createPeopleService(client);
                        return peopleService.people().get(resourceName)
                                .setPersonFields("names,emailAddresses,phoneNumbers")
                                .execute();
                    }

                    public Person updateContact(OAuth2AuthorizedClient client, String resourceName, Person person) throws IOException {
                        PeopleService peopleService = createPeopleService(client);
                        return peopleService.people().updateContact(resourceName, person)
                                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                                .execute();
                    }

                    public void deleteContact(OAuth2AuthorizedClient authorizedClient, String resourceName) throws IOException {
                        PeopleService peopleService = createPeopleService(authorizedClient);
                        peopleService.people().deleteContact(resourceName).execute();
                    }
                }