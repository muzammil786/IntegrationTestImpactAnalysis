class Changed {
    void f(String s) {
        boolean debug = true;
        if (debug) System.out.println("foo");
        if (s instanceof String) System.out.println("s: " + s);        

        if (true) { bar(); }

        String x = null;
    }
}
