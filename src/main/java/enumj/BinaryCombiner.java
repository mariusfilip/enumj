/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enumj;

/**
 *
 * @author Marius Filip
 */
@FunctionalInterface
public interface BinaryCombiner<U, V, W> {
    W combine(U u, V v);
}
