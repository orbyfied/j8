package net.orbyfied.j8.util.math.expr.error;

import net.orbyfied.j8.util.math.expr.StringLocation;

public interface LocatedException {

    StringLocation getLocation();

    LocatedException located(StringLocation loc);

}
