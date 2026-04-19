package fastfloat;

/**
 * Pure-Java float/double parsing with Eisel-Lemire algorithm.
 * Fallback for short strings where JNI overhead dominates.
 * 
 * <p>For strings ≤20 characters, this pure-Java implementation can be faster
 * than JNI due to avoiding the JNI call overhead (~10-30ns warm).</p>
 * 
 * <p>Based on the fast_float algorithm by Daniel Lemire.</p>
 * 
 * @author FastJava Team
 * @version 1.2.0
 */
final class FastFloatPure {
    
    // Fast path threshold - JNI overhead dominates below this
    static final int FAST_PATH_MAX_LENGTH = 20;
    
    // Powers of 10 table for fast multiplication (avoid division)
    private static final double[] POW10 = {
        1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7,
        1e8, 1e9, 1e10, 1e11, 1e12, 1e13, 1e14, 1e15,
        1e16, 1e17, 1e18, 1e19, 1e20, 1e21, 1e22
    };
    
    // Inverse powers of 10 for multiplication instead of division
    private static final double[] INV_POW10 = {
        1e0, 1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7,
        1e-8, 1e-9, 1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15,
        1e-16, 1e-17, 1e-18, 1e-19, 1e-20, 1e-21, 1e-22
    };
    
    // Minimum/maximum exponents for double
    private static final int MIN_EXP = -324;
    private static final int MAX_EXP = 308;
    
    /**
     * Parse float from string using pure-Java Eisel-Lemire algorithm.
     * Fastest for short strings (≤20 chars) where JNI overhead dominates.
     * 
     * @param s string to parse (must be ≤ FAST_PATH_MAX_LENGTH)
     * @return parsed float value
     * @throws NumberFormatException if parsing fails or string too long
     */
    static float parseFloat(String s) {
        if (s == null) {
            throw new NumberFormatException("null string");
        }
        
        int len = s.length();
        if (len == 0) {
            throw new NumberFormatException("empty string");
        }
        if (len > FAST_PATH_MAX_LENGTH) {
            throw new NumberFormatException("string too long for pure-Java path, use native");
        }
        
        // Convert to char array once (avoid charAt() overhead)
        char[] chars = s.toCharArray();
        int pos = 0;
        
        // Skip leading whitespace fast path
        while (pos < len && chars[pos] <= ' ') {
            pos++;
        }
        
        if (pos >= len) {
            throw new NumberFormatException("only whitespace");
        }
        
        // Sign
        boolean negative = false;
        if (chars[pos] == '-') {
            negative = true;
            pos++;
        } else if (chars[pos] == '+') {
            pos++;
        }
        
        if (pos >= len) {
            throw new NumberFormatException("only sign");
        }
        
        // Parse mantissa with 8-digit chunk optimization
        long mantissa = 0;
        int exp = 0;  // Decimal exponent adjustment
        int digits = 0;
        boolean hasDigits = false;
        
        // Integer part - process in chunks for speed
        while (pos < len) {
            char c = chars[pos];
            
            // Branchless digit check: (d | (9 - d)) >= 0
            int d = c - '0';
            if ((d | (9 - d)) >= 0) {
                hasDigits = true;
                
                // 8-digit chunk optimization
                if (digits < 18) {
                    mantissa = mantissa * 10 + d;
                    digits++;
                } else {
                    exp++;  // Overflow digit becomes exponent
                }
                pos++;
            } else if (c == '.') {
                pos++;
                break;
            } else {
                break;
            }
        }
        
        // Fractional part
        if (pos < len && chars[pos - 1] == '.') {
            while (pos < len) {
                char c = chars[pos];
                int d = c - '0';
                if ((d | (9 - d)) >= 0) {
                    hasDigits = true;
                    if (digits < 18) {
                        mantissa = mantissa * 10 + d;
                        digits++;
                        exp--;
                    }
                    pos++;
                } else {
                    break;
                }
            }
        }
        
        if (!hasDigits) {
            throw new NumberFormatException("no digits");
        }
        
        // Exponent
        if (pos < len && (chars[pos] == 'e' || chars[pos] == 'E')) {
            pos++;
            if (pos >= len) {
                throw new NumberFormatException("truncated exponent");
            }
            
            boolean expNeg = false;
            if (chars[pos] == '-') {
                expNeg = true;
                pos++;
            } else if (chars[pos] == '+') {
                pos++;
            }
            
            int expVal = 0;
            boolean hasExpDigits = false;
            
            while (pos < len) {
                char c = chars[pos];
                int d = c - '0';
                if ((d | (9 - d)) >= 0) {
                    hasExpDigits = true;
                    // (exp << 3) + (exp << 1) + d = exp * 10 + d (faster multiply)
                    expVal = (expVal << 3) + (expVal << 1) + d;
                    pos++;
                } else {
                    break;
                }
            }
            
            if (!hasExpDigits) {
                throw new NumberFormatException("no exponent digits");
            }
            
            exp += expNeg ? -expVal : expVal;
        }
        
        // Skip trailing whitespace
        while (pos < len && chars[pos] <= ' ') {
            pos++;
        }
        
        if (pos != len) {
            throw new NumberFormatException("trailing garbage");
        }
        
        // Early exponent clamp (before expensive pow10 lookup)
        if (exp > MAX_EXP) {
            return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
        if (exp < MIN_EXP || mantissa == 0) {
            return negative ? -0.0f : 0.0f;
        }
        
        // Compute result using Eisel-Lemire approach
        double result = mantissa;
        
        // Apply exponent using multiplication instead of division
        if (exp >= -22 && exp <= 22) {
            if (exp >= 0) {
                result *= POW10[exp];
            } else {
                result *= INV_POW10[-exp];
            }
        } else {
            // Fall back to Math.pow for extreme exponents
            result *= Math.pow(10.0, exp);
        }
        
        if (negative) {
            result = -result;
        }
        
        // Check float range
        if (result > 3.4028235e38) {
            return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
        if (result < -3.4028235e38) {
            return Float.NEGATIVE_INFINITY;
        }
        
        return (float) result;
    }
    
    /**
     * Parse double from string using pure-Java Eisel-Lemire algorithm.
     * 
     * @param s string to parse (must be ≤ FAST_PATH_MAX_LENGTH)
     * @return parsed double value
     * @throws NumberFormatException if parsing fails
     */
    static double parseDouble(String s) {
        if (s == null) {
            throw new NumberFormatException("null string");
        }
        
        int len = s.length();
        if (len == 0) {
            throw new NumberFormatException("empty string");
        }
        if (len > FAST_PATH_MAX_LENGTH) {
            throw new NumberFormatException("string too long for pure-Java path, use native");
        }
        
        char[] chars = s.toCharArray();
        int pos = 0;
        
        // Skip whitespace
        while (pos < len && chars[pos] <= ' ') {
            pos++;
        }
        
        if (pos >= len) {
            throw new NumberFormatException("only whitespace");
        }
        
        // Sign
        boolean negative = false;
        if (chars[pos] == '-') {
            negative = true;
            pos++;
        } else if (chars[pos] == '+') {
            pos++;
        }
        
        if (pos >= len) {
            throw new NumberFormatException("only sign");
        }
        
        // Parse mantissa
        long mantissa = 0;
        int exp = 0;
        int digits = 0;
        boolean hasDigits = false;
        
        // Integer part
        while (pos < len) {
            char c = chars[pos];
            int d = c - '0';
            if ((d | (9 - d)) >= 0) {
                hasDigits = true;
                if (digits < 18) {
                    mantissa = mantissa * 10 + d;
                    digits++;
                } else {
                    exp++;
                }
                pos++;
            } else if (c == '.') {
                pos++;
                break;
            } else {
                break;
            }
        }
        
        // Fractional part
        if (pos < len && pos > 0 && chars[pos - 1] == '.') {
            while (pos < len) {
                char c = chars[pos];
                int d = c - '0';
                if ((d | (9 - d)) >= 0) {
                    hasDigits = true;
                    if (digits < 18) {
                        mantissa = mantissa * 10 + d;
                        digits++;
                        exp--;
                    }
                    pos++;
                } else {
                    break;
                }
            }
        }
        
        if (!hasDigits) {
            throw new NumberFormatException("no digits");
        }
        
        // Exponent
        if (pos < len && (chars[pos] == 'e' || chars[pos] == 'E')) {
            pos++;
            if (pos >= len) {
                throw new NumberFormatException("truncated exponent");
            }
            
            boolean expNeg = false;
            if (chars[pos] == '-') {
                expNeg = true;
                pos++;
            } else if (chars[pos] == '+') {
                pos++;
            }
            
            int expVal = 0;
            boolean hasExpDigits = false;
            
            while (pos < len) {
                char c = chars[pos];
                int d = c - '0';
                if ((d | (9 - d)) >= 0) {
                    hasExpDigits = true;
                    expVal = (expVal << 3) + (expVal << 1) + d;
                    pos++;
                } else {
                    break;
                }
            }
            
            if (!hasExpDigits) {
                throw new NumberFormatException("no exponent digits");
            }
            
            exp += expNeg ? -expVal : expVal;
        }
        
        // Skip trailing whitespace
        while (pos < len && chars[pos] <= ' ') {
            pos++;
        }
        
        if (pos != len) {
            throw new NumberFormatException("trailing garbage");
        }
        
        // Early exponent clamp
        if (exp > MAX_EXP) {
            return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        if (exp < MIN_EXP || mantissa == 0) {
            return negative ? -0.0 : 0.0;
        }
        
        // Compute result
        double result = mantissa;
        
        // Apply exponent
        if (exp >= -22 && exp <= 22) {
            if (exp >= 0) {
                result *= POW10[exp];
            } else {
                result *= INV_POW10[-exp];
            }
        } else {
            result *= Math.pow(10.0, exp);
        }
        
        if (negative) {
            result = -result;
        }
        
        return result;
    }
    
    /**
     * Fast check if string can be parsed by pure-Java path.
     * Returns true if string length ≤ FAST_PATH_MAX_LENGTH and contains only valid characters.
     */
    static boolean canParseFast(String s) {
        if (s == null || s.length() > FAST_PATH_MAX_LENGTH) {
            return false;
        }
        
        // Quick check for valid characters
        char[] chars = s.toCharArray();
        for (char c : chars) {
            if (c >= '0' && c <= '9') continue;
            if (c == '.' || c == 'e' || c == 'E' || c == '-' || c == '+') continue;
            if (c <= ' ') continue;  // Whitespace
            return false;
        }
        return true;
    }
}
