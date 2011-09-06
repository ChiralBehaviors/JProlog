package com.hellblazer.prolog.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.kobe_u.cs.prolog.lang.ListTerm;
import jp.ac.kobe_u.cs.prolog.lang.Predicate;
import jp.ac.kobe_u.cs.prolog.lang.Prolog;
import jp.ac.kobe_u.cs.prolog.lang.PrologClassLoader;
import jp.ac.kobe_u.cs.prolog.lang.PrologControl;
import jp.ac.kobe_u.cs.prolog.lang.SymbolTerm;
import jp.ac.kobe_u.cs.prolog.lang.Term;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * 
 * @goalPrefix prolog
 * 
 * @phase generate-sources
 */
public class CompilerPlugin extends AbstractMojo {

    /** Copyright information */
    public static String COPYRIGHT = "Copyright(C) 1997-2009 M.Banbara and N.Tamura";
    /** Version information */
    public static String VERSION = "Prolog Cafe 1.2.5 (mantis)";

    private static final String PL = ".pl";

    private static final String PRO = ".pro";

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject mavenProject;

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}/generated-prolog/"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${basedir}/src/main/resources"
     * @required
     */
    private File resourceDirectory;

    /** Compiler option for arithmetic compilation. Its initial value is <code>true</code>
     * @parameter default-value="true"
     */
    protected boolean arithmeticCompilation = true;

    /** Compiler option for eliminating disjunctions. Its initial value is <code>true</code>
     * @parameter default-value="true"
     */
    protected boolean eliminateDisjunctions = true;

    /** Non-standard option. Compiler option for closure generation. Its initial value is <code>true</code> 
     * @parameter default-value="true"
     */
    protected boolean generateClosure = true;

    /** Compiler option for inline expansion. Its initial value is <code>true</code>
     * @parameter default-value="true"
     */
    protected boolean inlineExpansion = true;

    /** Compiler option for optimising recursive call. Its initial value is <code>true</code>
     * @parameter default-value="true"
     */
    protected boolean optimiseRecursiveCall = true;

    /** Compiler option for second-level indexing. Its initial value is <code>true</code>
     * @parameter default-value="true"
     */
    protected boolean switchOnHash = true;

    public void execute() throws MojoExecutionException {
        outputDirectory.mkdirs();

        mavenProject.addCompileSourceRoot(outputDirectory.getAbsolutePath());

        for (File pl : findPrologFiles(resourceDirectory)) {
            getLog().info("Translating: " + pl);
            File wam = new File(outputDirectory, pl.getName() + ".am");
            prologToJava(pl, wam, outputDirectory);
        }
    }

    /** 
     * Translates a Prolog program into Java programs.
     *
     * @param prolog an input Prolog file
     * @param dir a destination directory for java files. The directory must already exist.
     * @return <code>true</code> if succeeds, otherwise <code>false</code>.
     * @throws MojoExecutionException 
     * @see #prologToWAM(File, File)
     * @see #wamToJava(File, File)
    */
    public boolean prologToJava(File prolog, File wam, File dir) throws MojoExecutionException {
        if (!prologToWAM(prolog, wam)) {
            return false;
        }
        if (!wamToJava(wam, dir)) {
            return false;
        }
        return true;
    }

    /** 
     * Translates a Prolog program into a WAM-based intermediate code. 
     *
     * @param _prolog an input Prolog file
     * @param _wam an output file for WAM-based intermediate code. 
     * @return <code>true</code> if succeeds, otherwise <code>false</code>.
     * @throws MojoExecutionException 
    */
    public boolean prologToWAM(File _prolog, File _wam) throws MojoExecutionException {
        try {
            if (!_prolog.exists()) {
                getLog().error("file " + _prolog + " does not exist");
                return false;
            }
            if (_wam.exists()) {
                getLog().info("file " + _prolog + " already translated");
                return false;
            }
            // Create arguments
            Term prolog = SymbolTerm.makeSymbol(_prolog.getAbsolutePath());
            Term wam = SymbolTerm.makeSymbol(_wam.getAbsolutePath());
            Term op = Prolog.Nil;
            if (eliminateDisjunctions) {
                op = new ListTerm(SymbolTerm.makeSymbol("ed"), op);
            }
            if (arithmeticCompilation) {
                op = new ListTerm(SymbolTerm.makeSymbol("ac"), op);
            }
            if (inlineExpansion) {
                op = new ListTerm(SymbolTerm.makeSymbol("ie"), op);
            }
            if (optimiseRecursiveCall) {
                op = new ListTerm(SymbolTerm.makeSymbol("rc"), op);
            }
            if (switchOnHash) {
                op = new ListTerm(SymbolTerm.makeSymbol("idx"), op);
            }
            if (generateClosure) {
                op = new ListTerm(SymbolTerm.makeSymbol("clo"), op);
            }
            Term[] args = { new ListTerm(prolog,
                                         new ListTerm(wam,
                                                      new ListTerm(op,
                                                                   Prolog.Nil))) };
            // Create predicate
            PrologClassLoader prologClassLoader = new PrologClassLoader(
                                                                        getClass().getClassLoader());
            Class<?> clazz = prologClassLoader.loadPredicateClass(
                                                               "jp.ac.kobe_u.cs.prolog.compiler.pl2am",
                                                               "pl2am", 1, true);
            Predicate code = (Predicate) clazz.newInstance();
            // Translate Prolog into WAM
            PrologControl p = new PrologControl(prologClassLoader);
            p.setPredicate(code, args);
            return p.execute(code, args);
        } catch (Exception e) {
            getLog().error("Cannot translate " + _prolog, e);
            throw new MojoExecutionException("Cannot translate " + _prolog, e);
        }
    }

    /** 
     * Translates WAM-based intermediate code into Java programs.
     *
     * @param _wam an input file for WAM-based intermediate code. 
     * @param _dir a destination directory for java files. The directory must already exist.
     * @return <code>true</code> if succeeds, otherwise <code>false</code>.
     * @throws MojoExecutionException 
     * @see #prologToWAM(File, File)
    */
    public boolean wamToJava(File _wam, File _dir) throws MojoExecutionException {
        try {
            if (!_wam.exists()) {
                getLog().error("file " + _wam + " does not exist");
                return false;
            }
            if (!_dir.exists()) {
                getLog().error("directory " + _dir + " does not exist");
                return false;
            }
            // Create arguments
            Term wam = SymbolTerm.makeSymbol(_wam.getAbsolutePath());
            Term dir = SymbolTerm.makeSymbol(_dir.getAbsolutePath());
            Term[] args = { new ListTerm(wam, new ListTerm(dir, Prolog.Nil)) };
            // Create predicate
            //      Class clazz = PredicateEncoder.getClass("jp.ac.kobe_u.cs.prolog.compiler.am2j", "am2j", 1);
            PrologClassLoader prologClassLoader = new PrologClassLoader(
                                                                        getClass().getClassLoader());
            Class<?> clazz = prologClassLoader.loadPredicateClass(
                                                               "jp.ac.kobe_u.cs.prolog.compiler.am2j",
                                                               "am2j", 1, true);
            Predicate code = (Predicate) clazz.newInstance();
            // Translate WAM into Java
            PrologControl p = new PrologControl(prologClassLoader);
            p.setPredicate(code, args);
            return p.execute(code, args);
        } catch (Exception e) {
            getLog().error("Cannot translate wam " + _wam, e);
            throw new MojoExecutionException("Cannot translate wam " + _wam, e);
        }
    }

    private List<File> findPrologFiles(File directory) {
        List<File> files = new ArrayList<File>();
        if (!directory.exists()) {
            return files;
        }
        File[] filesAndDirs = directory.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        for (File file : filesDirs) {
            if (file.isFile()
                && (file.getName().endsWith(PL) || file.getName().endsWith(PRO))) {
                files.add(file);
            }
            if (file.isDirectory()) {
                List<File> deeperList = findPrologFiles(file);
                files.addAll(deeperList);
            }
        }
        return files;
    }
}
