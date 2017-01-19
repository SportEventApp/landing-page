package org.fmarin.tournoidevincennes.service;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpException;
import com.ecwid.maleorang.method.v3_0.lists.members.EditMemberMethod;
import org.fmarin.tournoidevincennes.domain.Prospect;
import org.fmarin.tournoidevincennes.repository.ProspectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
public class ProspectService {

    private final ProspectRepository repository;

    @Autowired
    public ProspectService(ProspectRepository repository) {
        this.repository = repository;
    }

    public Prospect createProspect(String email) throws IOException, MailchimpException {
        // Email already exists
        Optional<Prospect> isEmailRegistred = repository.findOneByEmail(email);
        if (!isEmailRegistred.isPresent()) {
            // Database registration
            Prospect prospect = new Prospect();
            prospect.setEmail(email);
            repository.save(prospect);
            // Mailchimp registration

            MailchimpClient mailchimpClient = new MailchimpClient("1cff0308c78788350f314f0f2f479fcc-us14");
            EditMemberMethod.Create subscribeProspectToListMethod = new EditMemberMethod.Create("eb7c2e54fd", email);
            subscribeProspectToListMethod.status = "subscribed";
            subscribeProspectToListMethod.timestamp_signup = Date.from(prospect.getCreatedDate().toInstant());
            mailchimpClient.execute(subscribeProspectToListMethod);
            // Mailchimp mail sent
            return prospect;
        }
        return null;
    }
}
