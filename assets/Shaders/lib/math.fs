
/**
  * Standard math functions
  */

/**
  * Eases in a value at a given power
  */
float easeInPow(float value, int power){
    return pow(value,power);
}

/**
  * Eases in a value at a given power
  */
float easeOutPow(float value, int power){
    return 1.0f - pow(value,power);
}
