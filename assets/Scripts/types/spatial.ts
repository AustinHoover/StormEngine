
/**
 * A 3D vector
 */
export class Vector {

    /**
     * The x coordinate
     */
    x: number

    /**
     * The y coordinate
     */
    y: number

    /**
     * The z coordinate
     */
    z: number

    /**
     * Gets the Vector as a string
     * @param input The vector to convert to a string
     * @returns The string
     */
    static toString(input: Vector): string {
        return Math.round(input.x * 100) / 100 + "," + Math.round(input.y * 100) / 100 + "," + Math.round(input.z * 100) / 100
    }

}
