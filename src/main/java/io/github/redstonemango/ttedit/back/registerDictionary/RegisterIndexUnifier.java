package io.github.redstonemango.ttedit.back.registerDictionary;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps track of the {@linkplain RegisterFileIndex} and the {@linkplain RegisterLiveIndex} and
 * combines both to a unified output.
 */
public class RegisterIndexUnifier {

    private final RegisterFileIndex fileIndex;
    private final RegisterLiveIndex liveIndex;
    private final Project project;
    private final Set<String> registers = new HashSet<>();

    private RegisterIndexUnifier(RegisterFileIndex fileIndex, RegisterLiveIndex liveIndex, Project project) {
        this.fileIndex = fileIndex;
        this.liveIndex = liveIndex;
        this.project = project;
    }

    public static RegisterIndexUnifier create(Project project) {
        RegisterFileIndex fileIndex = new RegisterFileIndex();
        RegisterLiveIndex liveIndex = new RegisterLiveIndex();
        RegisterIndexUnifier unifier = new RegisterIndexUnifier(fileIndex, liveIndex, project);
        fileIndex.initUnifier(unifier);
        liveIndex.initUnifier(unifier);
        return unifier;
    }

    void onRegisterChange() {
        Set<ProjectElement> openElements = liveIndex.getOpenElements();
        Stream<String> liveEntries = liveIndex.getEntriesStream();

        Stream<String> fileEntries = project.getElements().stream()
                .filter(e ->
                    e.getType() == ProjectElement.Type.SCRIPT
                    &&
                    !openElements.contains(e)
                )
                .map(fileIndex::getEntries)
                .flatMap(Set::stream);

        registers.clear();
        registers.addAll(
                Stream.concat(fileEntries, liveEntries)
                        .collect(Collectors.toSet())
        );
    }

    public Set<String> getRegisters() {
        return Collections.unmodifiableSet(registers);
    }

    public RegisterLiveIndex getLiveIndex() {
        return liveIndex;
    }

    public RegisterFileIndex getFileIndex() {
        return fileIndex;
    }

    public Project getProject() {
        return project;
    }
}
