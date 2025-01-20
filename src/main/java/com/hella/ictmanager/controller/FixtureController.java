package com.hella.ictmanager.controller;

import com.hella.ictmanager.entity.Fixture;
import com.hella.ictmanager.model.FixtureDTO;
import com.hella.ictmanager.repository.FixtureRepository;
import com.hella.ictmanager.service.FixtureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/fixtures")
public class FixtureController {

    private final FixtureService fixtureService;
    private final FixtureRepository fixtureRepository;

    public FixtureController(FixtureService fixtureService, FixtureRepository fixtureRepository) {
        this.fixtureService = fixtureService;
        this.fixtureRepository = fixtureRepository;
    }

    private static final String SAVE_FIXTURE = "insert fixture in Database";


    @GetMapping("/listEndpointsController")
    public String showFixtureControllerPage(Model model) {
        model.addAttribute("saveFixture", SAVE_FIXTURE);
        return "fixtureControllerForms/listEndpointsController";
    }

    @GetMapping("/adaptor/new")
    public String saveFixtureForm(Model model) {
        model.addAttribute("fixtureDTO", new FixtureDTO("", "", "", "", 0));
        return "fixtureControllerForms/saveFixture";
    }

    @PostMapping("/adaptor/save")
    public String saveFixture(@ModelAttribute FixtureDTO fixtureDTO, Model model) {
        fixtureService.save(fixtureDTO);
        model.addAttribute("message", "Fixture saved successfully");
        return "fixtureControllerForms/saveFixture";
    }


    @GetMapping("/findFixture")
    public String findFixture(Model model, @RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            try {
                FixtureDTO fixture = fixtureService.findById(id);
                model.addAttribute("fixture", fixture); // Adăugăm fixture-ul găsit în model
                model.addAttribute("message", "Fixture found");
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        } else {
            model.addAttribute("fixture", null); // Fără date inițiale dacă nu există ID-ul
        }
        return "fixtureControllerForms/findFixture"; // Afișează formularul de căutare a unui fixture
    }

    @GetMapping("/listFixtures")
    public String listFixtures(Model model) {
        List<Fixture> fixtures = fixtureRepository.findAll();
        model.addAttribute("fixtures", fixtures);
        return "fixtureControllerForms/listFixtures"; // Afișează formularul cu fixture-urile existente
    }

    @GetMapping("/updateFixture")
    public String updateFixtureForm(Model model) {
        model.addAttribute("fixture", new Fixture()); // Provide an empty Fixture object for the form
        return "fixtureControllerForms/updateFixture"; // Redirect to the update fixture form
    }

    @PostMapping("/updateFixture/IDForm")
    public String loadUpdateFixtureForm(@RequestParam("id") Long id, Model model) {
        try {
            Fixture fixture = fixtureService.findEntityById(id); // Get the fixture by ID
            model.addAttribute("fixture", fixture);
            return "fixtureControllerForms/updateFixture"; // Display the form populated with the fixture data
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "fixtureControllerForms/updateFixture"; // Show error message in the same form
        }
    }

    @PostMapping("/updateFixture")
    public String updateFixture(@ModelAttribute Fixture fixture, Model model) {
        try {
            fixtureService.update(fixture.getId(), FixtureDTO.convertToDTO(fixture));
            model.addAttribute("message", "Fixture updated successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "fixtureControllerForms/updateFixture";
    }

    @GetMapping("/deleteFixture")
    public String deleteFixtureForm(Model model) {
        model.addAttribute("fixture", new Fixture()); // Provide an empty Fixture object for the form
        return "fixtureControllerForms/deleteFixture"; // Redirect to the delete fixture form
    }

    @PostMapping("/deleteFixture")
    public String deleteFixture(@RequestParam("id") Long id, Model model) {
        try {
            fixtureService.removeFixtureFromMachine(id);
            fixtureService.deleteById(id);
            model.addAttribute("message", "Fixture deleted successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "fixtureControllerForms/deleteFixture";
    }

    @GetMapping("/addFixtureToMachine")
    public String showAddFixtureToMachineForm(Model model) {
        model.addAttribute("fixtures", fixtureRepository.findAll());
        return "fixtureControllerForms/addFixtureToMachine";
    }

    @PostMapping("/addFixtureToMachine")
    public String addFixtureToMachine(@RequestParam("fixtureId") Long fixtureId, @RequestParam("machineId") Long machineId, Model model) {
        try {
            fixtureService.addFixtureToMachine(fixtureId, machineId);
            model.addAttribute("message", "Fixture added to machine successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "fixtureControllerForms/addFixtureToMachine";
    }

    @GetMapping("/createMaintenanceReport")
    public String createMaintenanceReportForm() {
        return "fixtureControllerForms/createMaintenanceReport";
    }

    @PostMapping("/createMaintenanceReport")
    public String createMaintenanceReportForm(Model model) {
        try {
            fixtureService.createMaintenanceFixtureReport();
            model.addAttribute("message", "Maintenance report created successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "fixtureControllerForms/createMaintenanceReport";
    }
}
