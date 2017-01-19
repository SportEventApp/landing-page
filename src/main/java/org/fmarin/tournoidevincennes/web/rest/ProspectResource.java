package org.fmarin.tournoidevincennes.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.ecwid.maleorang.MailchimpException;
import org.fmarin.tournoidevincennes.domain.Prospect;
import org.fmarin.tournoidevincennes.service.ProspectService;
import org.fmarin.tournoidevincennes.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST controller for managing Prospect.
 */
@RestController
@RequestMapping("/api")
public class ProspectResource {

    private final Logger log = LoggerFactory.getLogger(ProspectResource.class);

    private final ProspectService prospectService;

    @Autowired
    public ProspectResource(ProspectService prospectService) {
        this.prospectService = prospectService;
    }

    /**
     * POST  /prospects : Create a new prospect.
     *
     * @param prospect the prospect to create
     * @return the ResponseEntity with status 201 (Created) and with body the new prospect, or with status 400 (Bad Request) if the prospect has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/prospects")
    @Timed
    public ResponseEntity<Prospect> createProspect(@Valid @RequestBody Prospect prospect) throws URISyntaxException {
        log.debug("REST request to save Prospect : {}", prospect);
        if (prospect.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("prospect", "idexists", "A new prospect cannot already have an ID")).body(null);
        }
        try {
            Prospect result = prospectService.createProspect(prospect.getEmail());
            return ResponseEntity.created(new URI("/api/prospects/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("prospect", result.getId().toString()))
                .body(result);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * PUT  /prospects : Updates an existing prospect.
//     *
//     * @param prospect the prospect to update
//     * @return the ResponseEntity with status 200 (OK) and with body the updated prospect,
//     * or with status 400 (Bad Request) if the prospect is not valid,
//     * or with status 500 (Internal Server Error) if the prospect couldnt be updated
//     * @throws URISyntaxException if the Location URI syntax is incorrect
//     */
//    @PutMapping("/prospects")
//    @Timed
//    public ResponseEntity<Prospect> updateProspect(@Valid @RequestBody Prospect prospect) throws URISyntaxException {
//        log.debug("REST request to update Prospect : {}", prospect);
//        if (prospect.getId() == null) {
//            return createProspect(prospect);
//        }
//        Prospect result = prospectRepository.save(prospect);
//        return ResponseEntity.ok()
//            .headers(HeaderUtil.createEntityUpdateAlert("prospect", prospect.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * GET  /prospects : get all the prospects.
//     *
//     * @return the ResponseEntity with status 200 (OK) and the list of prospects in body
//     */
//    @GetMapping("/prospects")
//    @Timed
//    public List<Prospect> getAllProspects() {
//        log.debug("REST request to get all Prospects");
//        List<Prospect> prospects = prospectRepository.findAll();
//        return prospects;
//    }
//
//    /**
//     * GET  /prospects/:id : get the "id" prospect.
//     *
//     * @param id the id of the prospect to retrieve
//     * @return the ResponseEntity with status 200 (OK) and with body the prospect, or with status 404 (Not Found)
//     */
//    @GetMapping("/prospects/{id}")
//    @Timed
//    public ResponseEntity<Prospect> getProspect(@PathVariable Long id) {
//        log.debug("REST request to get Prospect : {}", id);
//        Prospect prospect = prospectRepository.findOne(id);
//        return Optional.ofNullable(prospect)
//            .map(result -> new ResponseEntity<>(
//                result,
//                HttpStatus.OK))
//            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }
//
//    /**
//     * DELETE  /prospects/:id : delete the "id" prospect.
//     *
//     * @param id the id of the prospect to delete
//     * @return the ResponseEntity with status 200 (OK)
//     */
//    @DeleteMapping("/prospects/{id}")
//    @Timed
//    public ResponseEntity<Void> deleteProspect(@PathVariable Long id) {
//        log.debug("REST request to delete Prospect : {}", id);
//        prospectRepository.delete(id);
//        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("prospect", id.toString())).build();
//    }

}
