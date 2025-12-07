package io.github.redstonemango.ttedit.back.registerDictionary;

import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import javafx.scene.Node;

import java.util.*;
import java.util.stream.Stream;

/**
 * Tracks the current unsaved text's state of every node in a {@code Node → String}<br>
 * These entries override the {@linkplain  RegisterFileIndex} entries if the corresponding element is currently open.<br>
 * <br>
 * In addition, this class keeps track of which element the Node belongs to using a {@code ProjectElement -> Set<Node>} map.
 * The map contains open elements only.
 */
public class RegisterLiveIndex {

    private final Map<Node, String> liveRegisters = new HashMap<>();
    private final Map<ProjectElement, Set<Node>> nodeRegistry = new HashMap<>();
    private RegisterIndexUnifier unifier;

    void initUnifier(RegisterIndexUnifier unifier) {
        if (this.unifier != null) throw new IllegalStateException("An unifier is already registered");
        this.unifier = unifier;
    }

    public void updateEntry(Node node, ProjectElement ownerElement, String newRegister) {
        nodeRegistry.putIfAbsent(ownerElement, new HashSet<>());
        if (ScriptData.matchesRegisterPattern(newRegister)) {
            liveRegisters.put(node, newRegister);
            nodeRegistry.get(ownerElement).add(node);
        }
        else {
            nodeRegistry.getOrDefault(ownerElement, new HashSet<>()).remove(node);
            liveRegisters.remove(node);
        }
        unifier.onRegisterChange();
    }
    public String getEntry(Node node) {
        return liveRegisters.get(node);
    }
    public Stream<String> getEntriesStream() {
        return liveRegisters.values().stream();
    }
    public void closeElement(ProjectElement element) {
        nodeRegistry.getOrDefault(element, new HashSet<>()).forEach(liveRegisters::remove);
        nodeRegistry.remove(element);
        unifier.onRegisterChange();
    }
    public Set<ProjectElement> getOpenElements() {
        return Collections.unmodifiableSet(nodeRegistry.keySet());
    }

    public RegisterIndexUnifier getUnifier() {
        return unifier;
    }
}
