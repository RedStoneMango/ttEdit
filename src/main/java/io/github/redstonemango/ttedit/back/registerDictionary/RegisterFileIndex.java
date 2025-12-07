package io.github.redstonemango.ttedit.back.registerDictionary;

import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;

import java.util.*;

/**
 * Maintains a map of {@code ProjectElement → Set<String>} representing the saved register entries of every script element.
 */
public class RegisterFileIndex {

    private final Map<ProjectElement, Set<String>> savedRegisters = new HashMap<>();
    private RegisterIndexUnifier unifier;

    void initUnifier(RegisterIndexUnifier unifier) {
        if (this.unifier != null) throw new IllegalStateException("An unifier is already registered");
        this.unifier = unifier;
    }

    public void loadEntries(ProjectElement element) {
        updateEntries(element, new HashSet<>(element.getRegisters()));
    }
    public void updateEntries(ProjectElement element, Set<String> registers) {
        savedRegisters.put(element, registers);
        unifier.onRegisterChange();
    }
    public void removeEntries(ProjectElement element) {
        savedRegisters.remove(element);
        unifier.onRegisterChange();
    }
    public Set<String> getEntries(ProjectElement element) {
        return Collections.unmodifiableSet(savedRegisters.getOrDefault(element, new HashSet<>()));
    }

    public RegisterIndexUnifier getUnifier() {
        return unifier;
    }
}
