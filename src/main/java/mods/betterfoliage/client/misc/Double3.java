package mods.betterfoliage.client.misc;

import net.minecraftforge.common.util.ForgeDirection;

/** Immutable 3D vector of double precision.
 * @author octarine-noise
 */
public class Double3 {

	public final double x;
	public final double y;
	public final double z;
	
	public Double3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Double3(ForgeDirection dir) {
		this.x = dir.offsetX;
		this.y = dir.offsetY;
		this.z = dir.offsetZ;
	}
	
	public Double3 add(Double3 other) {
		return new Double3(x + other.x, y + other.y, z + other.z);
	}
	
	public Double3 sub(Double3 other) {
		return new Double3(x - other.x, y - other.y, z - other.z);
	}
	
	public Double3 add(double x, double y, double z) {
		return new Double3(this.x + x, this.y + y, this.z + z);
	}
	
	public Double3 scaleAxes(double sx, double sy, double sz) {
		return new Double3(x * sx, y * sy, z * sz);
	}
	
	public Double3 scale(double s) {
		return new Double3(x * s, y * s, z * s);
	}
	
	public Double3 inverse() {
		return new Double3(-x, -y, -z);
	}
}
