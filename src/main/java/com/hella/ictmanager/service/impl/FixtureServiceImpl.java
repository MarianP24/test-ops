package com.hella.ictmanager.service.impl;

import com.hella.ictmanager.entity.Fixture;
import com.hella.ictmanager.entity.Machine;
import com.hella.ictmanager.exception.FixtureFileNotFoundException;
import com.hella.ictmanager.model.FixtureDTO;
import com.hella.ictmanager.repository.FixtureRepository;
import com.hella.ictmanager.repository.MachineRepository;
import com.hella.ictmanager.service.FixtureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Component
public class FixtureServiceImpl implements FixtureService {
    private final FixtureRepository fixtureRepository;
    private final MachineRepository machineRepository;

    @Value("${server.path}")
    private String serverPath;

    public FixtureServiceImpl(FixtureRepository fixtureRepository, MachineRepository machineRepository) {
        this.fixtureRepository = fixtureRepository;
        this.machineRepository = machineRepository;
    }

    @Override
    public void save(FixtureDTO fixtureDTO) {
        fixtureRepository.save(fixtureDTO.convertToEntity());
        log.info("Fixture {} has been saved", fixtureDTO.fileName());
    }

    @Override
    public FixtureDTO findById(long id) {
        Fixture fixture = fixtureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fixture with id " + id + " not found"));
        log.info("Fixture {} has been found", fixture.getFileName());
        return FixtureDTO.convertToDTO(fixture);
    }

    @Override
    public List<FixtureDTO> findAll() {
        List<Fixture> fixtures = fixtureRepository.findAll();
        log.info("Found {} fixtures", fixtures.size());
        return fixtures.stream()
                .map(FixtureDTO::convertToDTO)
                .toList();
    }

    @Override
    public Fixture findEntityById(long id) {
        return fixtureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fixture with id " + id + " not found"));
    }


    @Override
    public void update(long id, FixtureDTO fixtureDTO) {
        Fixture oldFixture = fixtureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fixture with id " + id + " not found"));
        oldFixture.setFileName(fixtureDTO.fileName());
        oldFixture.setBusiness(fixtureDTO.business());
        oldFixture.setProductName(fixtureDTO.productName());
        oldFixture.setProgramName(fixtureDTO.programName());
        oldFixture.setFixtureCounterSet(fixtureDTO.fixtureCounterSet());
        fixtureRepository.save(oldFixture);
        log.info("Fixture {} has been updated", fixtureDTO.fileName());
    }

    @Override
    public void deleteById(long id) {
        fixtureRepository.deleteById(id);
        log.info("Fixture with id {} has been deleted", id);
    }

    @Override
    public void addFixtureToMachine(long fixtureId, long machineId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture with id " + fixtureId + " not found"));
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine with id " + machineId + " not found"));

        if (fixture.getMachines().contains(machine)) {
            log.info("Fixture {} is already associated with machine {}", fixture.getFileName(), machine.getEquipmentName());
            return;
        }
        fixture.getMachines().add(machine);

        fixtureRepository.save(fixture);
        log.info("Fixture {} has been added to machine {}", fixture.getFileName(), machine.getEquipmentName());
    }

    public void createMaintenanceFixtureReport() {
        List<Fixture> fixtures = fixtureRepository.findAll();

        for (Fixture fixture : fixtures) {
            log.info("Fixture {} has been reported for maintenance", fixture.getFileName());
            doBusinessLogic(fixture);
        }
        log.info("Maintenance report has been created");
    }

    private File createFixtureFile(Fixture fixture) {
        File file = new File(serverPath + fixture.getFileName());

        if (!file.exists()) {
            throw new IllegalArgumentException("File " + fixture.getFileName() + " does not exist");
        }
        log.info("File {} has been found in path {}", fixture.getFileName(), file.getAbsolutePath());
        log.info("File {} has been created", fixture.getFileName());
        return file;
    }

    private void doBusinessLogic(Fixture fixture) {
        File file = createFixtureFile(fixture);

        try (Scanner scanner = new Scanner(file)) {

            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                log.info("Line {} has been read from fixture {}", line, fixture.getFileName());
                String[] words = line.split("\\s+");
                int counter = Integer.parseInt(words[0]);

                if (counter >= fixture.getFixtureCounterSet()) {
                    resetCounter(fixture.getFileName(), file.getAbsolutePath());
                    log.info("Counter has been reset for fixture {}", fixture.getFileName());
                } else {
                    fixture.setCounter(Integer.parseInt(words[0]));
                    fixtureRepository.save(fixture);
                    log.info("Counter has been updated for fixture {}", fixture.getFileName());
                }
            }
        } catch (FileNotFoundException e) {
            throw new FixtureFileNotFoundException(fixture.getFileName() + "Not found", e);
        }
    }

    private void resetCounter(String fixtureFileName, String filePath) {
        String countersFileName = "contoare resetate.txt";
        try (FileWriter wtgFileWriter = new FileWriter(filePath);
             FileWriter countersFileWriter = new FileWriter(serverPath + countersFileName, true)) {

            String newline = "0 0 n";
            wtgFileWriter.write(newline);
            countersFileWriter.write("Contorul fixture-ului " + fixtureFileName + " a fost resetat la 0 in data de: " + java.time.LocalDate.now() + "\n");
        } catch (IOException e) {
            log.error("An error occurred while resetting the counter", e);
        }
    }

    @Scheduled(cron = "0 45 13 * * ?")
    public void scheduleBusinessLogic() {
        List<Fixture> fixtures = fixtureRepository.findAll();
        for (Fixture fixture : fixtures) {
            doBusinessLogic(fixture);
        }
    }

    @Override
    public void removeFixtureFromMachine(long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture with id " + fixtureId + " not found"));
        fixture.getMachines().clear();
        fixtureRepository.save(fixture);
    }
}
