package com.capdevon.physx;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

/**
 * 
 * @author capdevon
 */
public class Physics {
	
    /**
     * DefaultRaycastLayers ALL LAYERS
     */
    private static final int DefaultRaycastLayers = ~0;
    
    private Physics() {
        // private constructor.
    }
    
    /**
     * Applies a force to a rigidbody that simulates explosion effects.
     * 
     * @param rb                The rigidbody object.
     * @param explosionForce    The force of the explosion (which may be modified by distance).
     * @param explosionPosition The centre of the sphere within which the explosion has its effect.
     * @param explosionRadius   The radius of the sphere within which the explosion has its effect.
     */
    public static void addExplosionForce(PhysicsRigidBody rb, float explosionForce, Vector3f explosionPosition, float explosionRadius) {
        Vector3f expCenter2Body = rb.getPhysicsLocation().subtract(explosionPosition);
        float distance = expCenter2Body.length();
        if (distance < explosionRadius) {
            // apply proportional explosion force
            float strength = (1.f - FastMath.clamp(distance / explosionRadius, 0, 1)) * explosionForce;
            rb.setLinearVelocity(expCenter2Body.normalize().mult(strength));
        }
    }
    
    /**
     * Casts a ray through the scene and returns all hits.
     */
    public static List<RaycastHit> raycastAll(Ray ray, float maxDistance) {
        return raycastAll(ray, maxDistance, DefaultRaycastLayers);
    }

    /**
     * Casts a ray through the scene and returns all hits.
     * 
     * @param ray         The starting point and direction of the ray.
     * @param maxDistance The max distance the rayhit is allowed to be from the start of the ray.
     * @param layerMask   A Layer mask that is used to selectively ignore colliders when casting a ray.
     * @return A list of RaycastHit objects.
     */
    public static List<RaycastHit> raycastAll(Ray ray, float maxDistance, int layerMask) {

        List<RaycastHit> lstResults = new ArrayList<>();

        TempVars t = TempVars.get();
        Vector3f beginVec = t.vect1.set(ray.origin);
        Vector3f finalVec = t.vect2.set(ray.direction).multLocal(maxDistance).addLocal(ray.origin);

        List<PhysicsRayTestResult> results = PhysicsSpace.getPhysicsSpace().rayTest(beginVec, finalVec);

        for (PhysicsRayTestResult phRay : results) {
            PhysicsCollisionObject pco = phRay.getCollisionObject();

            if (phRay.getHitFraction() < maxDistance && applyMask(layerMask, pco.getCollisionGroup())) {

                RaycastHit hitInfo = new RaycastHit();
                hitInfo.rigidBody   = pco;
                hitInfo.collider    = pco.getCollisionShape();
                hitInfo.gameObject  = pco.getUserObject();
                hitInfo.normal      = phRay.getHitNormalLocal();
                hitInfo.distance    = finalVec.subtract(beginVec).length() * phRay.getHitFraction();
                hitInfo.point.interpolateLocal(beginVec, finalVec, phRay.getHitFraction());

                lstResults.add(hitInfo);
            }
        }

        t.release();
        return lstResults;
    }
    
    /**
     * Casts a ray, from point origin, in direction direction, of length
     * maxDistance, against all colliders in the Scene.
     */
    public static boolean raycast(Vector3f origin, Vector3f direction, RaycastHit hitInfo, float maxDistance) {
        return raycast(origin, direction, hitInfo, maxDistance, DefaultRaycastLayers);
    }
    
    public static boolean raycast(Ray ray, RaycastHit hitInfo, float maxDistance) {
        return raycast(ray.origin, ray.direction, hitInfo, maxDistance, DefaultRaycastLayers);
    }
    
    /**
     * Casts a ray, from point origin, in direction direction, of length
     * maxDistance, against all colliders in the Scene.
     * 
     * @param origin      The starting point of the ray in world coordinates. (not null, unaffected)
     * @param direction   The direction of the ray. (not null, unaffected)
     * @param hitInfo     If true is returned, hitInfo will contain more information
     *                    about where the closest collider was hit. (See Also: RaycastHit).
     * @param maxDistance The max distance the ray should check for collisions.
     * @param layerMask   A Layer mask that is used to selectively ignore Colliders when casting a ray.
     * @return Returns true if the ray intersects with a Collider, otherwise false.
     */
    public static boolean raycast(Vector3f origin, Vector3f direction, RaycastHit hitInfo, float maxDistance, int layerMask) {
        
        boolean collision = false;
        float hf = maxDistance;

        TempVars t = TempVars.get();
        Vector3f beginVec = t.vect1.set(origin);
        Vector3f finalVec = t.vect2.set(direction).multLocal(maxDistance).addLocal(origin);

        List<PhysicsRayTestResult> results = PhysicsSpace.getPhysicsSpace().rayTest(beginVec, finalVec);

        for (PhysicsRayTestResult ray : results) {
            PhysicsCollisionObject pco = ray.getCollisionObject();

            if (ray.getHitFraction() < hf && applyMask(layerMask, pco.getCollisionGroup())) {
                collision = true;
                hf = ray.getHitFraction();

                hitInfo.rigidBody   = pco;
                hitInfo.collider    = pco.getCollisionShape();
                hitInfo.gameObject  = pco.getUserObject();
                hitInfo.normal      = ray.getHitNormalLocal();
                hitInfo.distance    = finalVec.subtract(beginVec).length() * hf;
                hitInfo.point.interpolateLocal(beginVec, finalVec, hf);
            }
        }

        if (!collision) {
            hitInfo.clear();
        }

        t.release();
        return collision;
    }
    
    /**
     * Returns true if there is any collider intersecting the line between beginVec and finalVec.
     */
    public static boolean linecast(Vector3f beginVec, Vector3f finalVec, RaycastHit hitInfo) {
        return linecast(beginVec, finalVec, hitInfo, DefaultRaycastLayers);
    }
    
    /**
     * Returns true if there is any collider intersecting the line between beginVec and finalVec.
     * 
     * @param beginVec  (not null, unaffected)
     * @param finalVec  (not null, unaffected)
     * @param hitInfo   If true is returned, hitInfo will contain more information
     *                  about where the closest collider was hit. (See Also: RaycastHit).
     * @param layerMask A Layer mask that is used to selectively ignore Colliders when casting a ray.
     * @return Returns true if the ray intersects with a Collider, otherwise false.
     */
    public static boolean linecast(Vector3f beginVec, Vector3f finalVec, RaycastHit hitInfo, int layerMask) {

        boolean collision = false;
        float hf = finalVec.length();

        List<PhysicsRayTestResult> results = PhysicsSpace.getPhysicsSpace().rayTest(beginVec, finalVec);
        for (PhysicsRayTestResult ray : results) {

            PhysicsCollisionObject pco = ray.getCollisionObject();

            if (ray.getHitFraction() < hf && applyMask(layerMask, pco.getCollisionGroup())) {
                collision = true;
                hf = ray.getHitFraction();

                hitInfo.rigidBody   = pco;
                hitInfo.collider    = pco.getCollisionShape();
                hitInfo.gameObject  = pco.getUserObject();
                hitInfo.normal      = ray.getHitNormalLocal();
                hitInfo.point       = FastMath.interpolateLinear(hf, beginVec, finalVec);
                hitInfo.distance    = beginVec.distance(hitInfo.point);
            }
        }

        if (!collision) {
            hitInfo.clear();
        }

        return collision;
    }

    /**
     * Check if a collisionGroup is in a layerMask
     *
     * @param layerMask
     * @param collisionGroup
     * @return
     */
    private static boolean applyMask(int layerMask, int collisionGroup) {
        return layerMask == (layerMask | collisionGroup);
    }
}
