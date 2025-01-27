package com.capdevon.engine;

import java.util.ArrayList;
import java.util.List;

import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/**
 *
 * @author capdevon
 */
public class GameObject {

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private GameObject() {
    }

    /**
     * Returns all components of Type type in the GameObject.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T extends Control> T[] getComponents(Spatial spatial, Class<T> clazz) {
        final List<Node> lst = new ArrayList<>(5);
        spatial.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (node.getControl(clazz) != null) {
                    lst.add(node);
                }
            }
        });
        return (T[]) lst.toArray();
    }

    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
     *
     * @param <T>
     * @param spatial
     * @param clazz
     * @return
     */
    public static <T extends Control> T getComponentInChildren(Spatial spatial, Class<T> clazz) {
        T control = spatial.getControl(clazz);
        if (control != null) {
            return control;
        }

        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                control = getComponentInChildren(child, clazz);
                if (control != null) {
                    return control;
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the component of Type type in the GameObject or any of its
     * parents.
     *
     * @param <T>
     * @param spatial
     * @param clazz
     * @return
     */
    public static <T extends Control> T getComponentInParent(Spatial spatial, Class<T> clazz) {
        Node parent = spatial.getParent();
        while (parent != null) {
            T control = parent.getControl(clazz);
            if (control != null) {
                return control;
            }
            parent = parent.getParent();
        }
        return null;
    }

}
