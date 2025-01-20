package com.hella.ictmanager.controller;

import com.hella.ictmanager.entity.Machine;
import com.hella.ictmanager.model.MachineDTO;
import com.hella.ictmanager.service.impl.MachineServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/machines")
public class MachineController {
    private final MachineServiceImpl machineService;

    public MachineController(MachineServiceImpl machineService) {
        this.machineService = machineService;
    }

    @GetMapping("/listEndpointsMachine")
    public String showMachineControllerPage() {
        return "machineControllerForms/listEndpointsMachine";
    }

    @GetMapping("/machine/new")
    public String saveMachineForm(Model model) {
        model.addAttribute("machine", new Machine());
        return "machineControllerForms/saveMachine";
    }

    @PostMapping("/machine/save")
    public String saveMachine(@ModelAttribute MachineDTO machineDTO, Model model) {
        machineService.save(machineDTO);
        model.addAttribute("message", "Machine saved successfully");
        return "machineControllerForms/saveMachine";
    }

    @GetMapping("/findMachine")
    public String findMachine(Model model, @RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            try {
                MachineDTO machine = machineService.findById(id);
                model.addAttribute("machine", machine);
                model.addAttribute("message", "Machine found");
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        } else {
            model.addAttribute("errorMessage", "Please provide an id");
        }
        return "machineControllerForms/findMachine";
    }

    @GetMapping("/listMachines")
    public String listMachines(Model model) {
        List<MachineDTO> machines = machineService.findAll();
        model.addAttribute("machines", machines);
        return "machineControllerForms/listMachines";
    }

    @GetMapping("/updateMachine")
    public String updateMachineForm(Model model) {
        model.addAttribute("machine", new Machine());
        return "machineControllerForms/updateMachine";
    }

    @PostMapping("/updateMachine/IDForm")
    public String loadUpdateMachineForm(@RequestParam("id") Long id, Model model) {
        try {
            MachineDTO machine = machineService.findById(id);
            model.addAttribute("machine", machine);
            model.addAttribute("message", "Machine found");
            return "machineControllerForms/updateMachine";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "machineControllerForms/updateMachine";
        }
    }

    @PostMapping("/updateMachine/save")
    public String updateMachine(@ModelAttribute Machine machine, Model model) {
        try {
            machineService.update(machine.getId(), MachineDTO.convertToDTO(machine));
            model.addAttribute("message", "Machine updated successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "machineControllerForms/updateMachine";
    }

    @GetMapping("/deleteMachine")
    public String deleteMachineForm(Model model) {
        model.addAttribute("machine", new Machine());
        return "machineControllerForms/deleteMachine";
    }

    @PostMapping("/deleteMachine")
    public String deleteMachine(@RequestParam("id") Long id, Model model) {
        try {
            machineService.deleteById(id);
            model.addAttribute("message", "Machine deleted successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "machineControllerForms/deleteMachine";
    }
}