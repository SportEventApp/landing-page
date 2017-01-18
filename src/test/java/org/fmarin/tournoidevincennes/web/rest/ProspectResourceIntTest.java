package org.fmarin.tournoidevincennes.web.rest;

import org.fmarin.tournoidevincennes.TournoidevincennesApp;

import org.fmarin.tournoidevincennes.domain.Prospect;
import org.fmarin.tournoidevincennes.repository.ProspectRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ProspectResource REST controller.
 *
 * @see ProspectResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournoidevincennesApp.class)
public class ProspectResourceIntTest {

    private static final String DEFAULT_EMAIL = "aaaa@aa.com";
    private static final String UPDATED_EMAIL = "bbbb@bb.com";

    @Inject
    private ProspectRepository prospectRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restProspectMockMvc;

    private Prospect prospect;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ProspectResource prospectResource = new ProspectResource();
        ReflectionTestUtils.setField(prospectResource, "prospectRepository", prospectRepository);
        this.restProspectMockMvc = MockMvcBuilders.standaloneSetup(prospectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prospect createEntity(EntityManager em) {
        Prospect prospect = new Prospect()
                .email(DEFAULT_EMAIL);
        return prospect;
    }

    @Before
    public void initTest() {
        prospect = createEntity(em);
    }

    @Test
    @Transactional
    public void createProspect() throws Exception {
        int databaseSizeBeforeCreate = prospectRepository.findAll().size();

        // Create the Prospect

        restProspectMockMvc.perform(post("/api/prospects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(prospect)))
            .andExpect(status().isCreated());

        // Validate the Prospect in the database
        List<Prospect> prospectList = prospectRepository.findAll();
        assertThat(prospectList).hasSize(databaseSizeBeforeCreate + 1);
        Prospect testProspect = prospectList.get(prospectList.size() - 1);
        assertThat(testProspect.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    public void createProspectWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = prospectRepository.findAll().size();

        // Create the Prospect with an existing ID
        Prospect existingProspect = new Prospect();
        existingProspect.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProspectMockMvc.perform(post("/api/prospects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingProspect)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Prospect> prospectList = prospectRepository.findAll();
        assertThat(prospectList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = prospectRepository.findAll().size();
        // set the field null
        prospect.setEmail(null);

        // Create the Prospect, which fails.

        restProspectMockMvc.perform(post("/api/prospects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(prospect)))
            .andExpect(status().isBadRequest());

        List<Prospect> prospectList = prospectRepository.findAll();
        assertThat(prospectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllProspects() throws Exception {
        // Initialize the database
        prospectRepository.saveAndFlush(prospect);

        // Get all the prospectList
        restProspectMockMvc.perform(get("/api/prospects?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prospect.getId().intValue())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL.toString())));
    }

    @Test
    @Transactional
    public void getProspect() throws Exception {
        // Initialize the database
        prospectRepository.saveAndFlush(prospect);

        // Get the prospect
        restProspectMockMvc.perform(get("/api/prospects/{id}", prospect.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(prospect.getId().intValue()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingProspect() throws Exception {
        // Get the prospect
        restProspectMockMvc.perform(get("/api/prospects/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProspect() throws Exception {
        // Initialize the database
        prospectRepository.saveAndFlush(prospect);
        int databaseSizeBeforeUpdate = prospectRepository.findAll().size();

        // Update the prospect
        Prospect updatedProspect = prospectRepository.findOne(prospect.getId());
        updatedProspect
                .email(UPDATED_EMAIL);

        restProspectMockMvc.perform(put("/api/prospects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedProspect)))
            .andExpect(status().isOk());

        // Validate the Prospect in the database
        List<Prospect> prospectList = prospectRepository.findAll();
        assertThat(prospectList).hasSize(databaseSizeBeforeUpdate);
        Prospect testProspect = prospectList.get(prospectList.size() - 1);
        assertThat(testProspect.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void updateNonExistingProspect() throws Exception {
        int databaseSizeBeforeUpdate = prospectRepository.findAll().size();

        // Create the Prospect

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restProspectMockMvc.perform(put("/api/prospects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(prospect)))
            .andExpect(status().isCreated());

        // Validate the Prospect in the database
        List<Prospect> prospectList = prospectRepository.findAll();
        assertThat(prospectList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteProspect() throws Exception {
        // Initialize the database
        prospectRepository.saveAndFlush(prospect);
        int databaseSizeBeforeDelete = prospectRepository.findAll().size();

        // Get the prospect
        restProspectMockMvc.perform(delete("/api/prospects/{id}", prospect.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Prospect> prospectList = prospectRepository.findAll();
        assertThat(prospectList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
