class Changed {
    void f(String s) {
        boolean debug = true;
        if (s instanceof String) System.out.println("s: " + s);

        if (true) { foo(); }

        String x = null;
        String y = null;
    }
}
