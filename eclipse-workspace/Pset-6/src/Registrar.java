import tester.Tester;

// interface for a generic list
interface IList<T> {

  // accepts a visitor to this list
  <R> R accept(IListVisitor<T, R> visitor);

  // performs a function on every item in this list
  <U> IList<U> map(IFunc<T, U> func);

  // implementation of foldr
  <U> U foldr(IFunc2<T, U, U> func, U base);

}

// generic cons list
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // accepts a visitor to this ConsList
  public <R> R accept(IListVisitor<T, R> visitor) {
    return visitor.visitCons(this);
  }

  // maps a function over a ConsList
  public <U> IList<U> map(IFunc<T, U> func) {
    return new ConsList<U>(func.apply(this.first), this.rest.map(func));
  }

  // implements foldr onto this ConsList
  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return func.apply(this.first, this.rest.foldr(func, base));
  }
}

// generic empty list
class MtList<T> implements IList<T> {

  // accepts a visitor to this ConsList
  public <R> R accept(IListVisitor<T, R> visitor) {
    return visitor.visitMt(this);
  }

  // maps a function over an empty list
  public <U> IList<U> map(IFunc<T, U> func) {
    return new MtList<U>();
  }

  // implements foldr onto an empty list
  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return base;
  }
}

// interface for a 1 input function
interface IFunc<A, R> {
  R apply(A arg);
}

//interface for a functio that takes in 2 inputs
interface IFunc2<A1, A2, R> {
  R apply(A1 input1, A2 input2);
}

// visitors for generic lists
interface IListVisitor<T, R> extends IFunc<IList<T>, R> {

  // applies an operation to a empty list of courses
  R visitMt(MtList<T> arg);

  // applies an operation to a non empty list of courses
  R visitCons(ConsList<T> arg);

}

// interface for predicate functions
interface IPred<T> extends IFunc<T, Boolean> {
  Boolean apply(T t);
}

class Ormap<T> implements IListVisitor<T, Boolean> {
  IPred<T> predicate;

  Ormap(IPred<T> predicate) {
    this.predicate = predicate;
  }

  // applies the ormap
  public Boolean apply(IList<T> arg) {
    return arg.accept(this);
  }

  // ormaps over an empty list
  public Boolean visitMt(MtList<T> arg) {
    return false;
  }

  // ormaps over a non empty list
  public Boolean visitCons(ConsList<T> arg) {
    return this.predicate.apply(arg.first) || arg.rest.accept(this);
  }
}

// class representing a university course
class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  Course(String name, Instructor prof) {
    this(name, prof, new MtList<Student>());
  }

  Course(String name, Instructor prof, IList<Student> students) {
    this.name = name;
    this.prof = prof;
    this.prof.updateCourses(this);
    this.students = students;
  }
}

// represents an instructor of a course
class Instructor {
  String name;
  IList<Course> courses;

  Instructor(String name) {
    this(name, new MtList<Course>());
  }

  public void updateCourses(Course course) {
    this.courses = new ConsList<Course>(course, this.courses);

  }

  Instructor(String name, IList<Course> courses) {
    this.name = name;
    this.courses = courses;
  }

  public boolean sameProf(Instructor prof) {
    return this.name.equals(prof.name);
  }

  public boolean dejavu(Student s) {
    return new CountStudentAppearancesVisitor(s).apply(this.courses) > 1;
  }
}

// represents a student
class Student {
  String name;
  int id;
  IList<Course> courses;

  Student(String name, int id) {
    this(name, id, new MtList<Course>());
  }

  Student(String name, int id, IList<Course> courses) {
    this.name = name;
    this.id = id;
    this.courses = courses;
  }

  // Effect: enrolls a student in a course
  public void enroll(Course c) {
    this.courses = new ConsList<Course>(c, courses);
    c.students = new ConsList<Student>(this, c.students);
  }

  // checks if the passed in student is classmates with this student
  public boolean classmates(Student s) {
    return new CountStudentAppearancesVisitor(s).apply(this.courses) >= 1;
  }
}

// counts the number of times a passed in student apppears as a student
// in a list of courses
class CountStudentAppearancesVisitor implements IListVisitor<Course, Integer> {
  Student student;

  CountStudentAppearancesVisitor(Student student) {
    this.student = student;
  }

  // accepts the visitor
  public Integer apply(IList<Course> arg) {
    return arg.accept(this);
  }

  // visits an empty list of Courses;
  public Integer visitMt(MtList<Course> arg) {
    return 0;
  }

  // counts how many times the student appears in a list of courses's students list
  public Integer visitCons(ConsList<Course> arg) {
    if (new Ormap<Student>(new SameStudent(this.student)).apply(arg.first.students)) {
      return 1 + new CountStudentAppearancesVisitor(this.student).apply(arg.rest);
    }
    else {
      return new CountStudentAppearancesVisitor(this.student).apply(arg.rest);
    }
  }

}

class SameStudent implements IPred<Student> {
  Student student;

  SameStudent(Student student) {
    this.student = student;
  }

  public Boolean apply(Student thatStudent) {
    return this.student.name.equals(thatStudent.name) && this.student.id == thatStudent.id;
  }
}

// tests
class ExamplesRegistrar {
  Student s1 = new Student("s1", 01, new MtList<Course>());
  Student s2 = new Student("s2", 02, new MtList<Course>());
  Student s3 = new Student("s3", 03, new MtList<Course>());
  Student s4 = new Student("s4", 04, new MtList<Course>());
  Student s5 = new Student("s5", 05, new MtList<Course>());

  Instructor i1 = new Instructor("i1", new MtList<Course>());
  Instructor i2 = new Instructor("i2", new MtList<Course>());

  Course c1 = new Course("c1", this.i1, new MtList<Student>());
  Course c2 = new Course("c2", this.i1, new MtList<Student>());
  Course c3 = new Course("c3", this.i2, new MtList<Student>());
  Course c4 = new Course("c4", this.i2, new MtList<Student>());

  public void reset() {
    this.s1 = new Student("s1", 01, new MtList<Course>());
    this.s2 = new Student("s2", 02, new MtList<Course>());
    this.s3 = new Student("s3", 03, new MtList<Course>());
    this.s4 = new Student("s4", 04, new MtList<Course>());
    this.s5 = new Student("s5", 05, new MtList<Course>());

    this.c1 = new Course("c1", i1, new MtList<Student>());
    this.c2 = new Course("c2", i1, new MtList<Student>());
    this.c3 = new Course("c3", i2, new MtList<Student>());
    this.c4 = new Course("c4", i2, new MtList<Student>());

    this.i1 = new Instructor("i1",
        new ConsList<Course>(c1, new ConsList<Course>(c2, new MtList<Course>())));
    this.i2 = new Instructor("i2", new MtList<Course>());
  }

  // TODO: make sure to retstrain the data constraints
  // TODO: dont enroll a studnet if they are already enrolled
  // maybe the functio takes in a list of courses and records how many times the
  // studnt appears in the studnet lists

  // tests for enroll

  void testEnroll(Tester t) {
    reset();
    t.checkExpect(s1.courses, new MtList<Course>());
    t.checkExpect(c1.students, new MtList<Student>());
    this.s1.enroll(this.c1);
    t.checkExpect(s1.courses, new ConsList<Course>(c1, new MtList<Course>()));
    t.checkExpect(c1.students, new ConsList<Student>(s1, new MtList<Student>()));
    this.s1.enroll(this.c2);
    t.checkExpect(s1.courses,
        new ConsList<Course>(c2, new ConsList<Course>(c1, new MtList<Course>())));
    t.checkExpect(c2.students, new ConsList<Student>(s1, new MtList<Student>()));
    this.s2.enroll(this.c1);
    t.checkExpect(s2.courses, new ConsList<Course>(c1, new MtList<Course>()));
    t.checkExpect(c1.students,
        new ConsList<Student>(s2, new ConsList<Student>(s1, new MtList<Student>())));
  }

  // test for SameProf
  void testSameProf(Tester t) {
    reset();
    t.checkExpect(this.i1.sameProf(this.i1), true);
    t.checkExpect(this.i1.sameProf(this.i2), false);
  }

  // tests for classmates
  void testClassmates(Tester t) {
    reset();
    t.checkExpect(this.s1.classmates(this.s2), false);
    t.checkExpect(this.s2.classmates(this.s1), false);
    this.s1.enroll(this.c1);
    t.checkExpect(this.s1.classmates(this.s2), false);
    t.checkExpect(this.s2.classmates(this.s1), false);
    this.s2.enroll(this.c1);
    t.checkExpect(this.s1.classmates(this.s2), true);
    t.checkExpect(this.s2.classmates(this.s1), true);
    reset();
    t.checkExpect(this.s1.classmates(this.s2), false);
    t.checkExpect(this.s2.classmates(this.s1), false);
    this.s2.enroll(this.c1);
    t.checkExpect(this.s1.classmates(this.s2), false);
    t.checkExpect(this.s2.classmates(this.s1), false);
    this.s1.enroll(this.c1);
    t.checkExpect(this.s1.classmates(this.s2), true);
    t.checkExpect(this.s2.classmates(this.s1), true);
    reset();
  }

  // test for CountStudentAppearancesVisitor
  void testCountStudentAppearancesVisitor(Tester t) {
    reset();
    t.checkExpect(new CountStudentAppearancesVisitor(s1)
        .apply(new ConsList<Course>(c1, new MtList<Course>())), 0);
    t.checkExpect(new CountStudentAppearancesVisitor(s1).apply(new MtList<Course>()), 0);
    s1.enroll(c1);
    t.checkExpect(new CountStudentAppearancesVisitor(s1)
        .apply(new ConsList<Course>(c2, new MtList<Course>())), 0);
    t.checkExpect(new CountStudentAppearancesVisitor(s1)
        .apply(new ConsList<Course>(c1, new MtList<Course>())), 1);
    s1.enroll(c2);
    t.checkExpect(new CountStudentAppearancesVisitor(s1)
        .apply(new ConsList<Course>(c2, new MtList<Course>())), 1);
    t.checkExpect(new CountStudentAppearancesVisitor(s1)
        .apply(new ConsList<Course>(c1, new MtList<Course>())), 1);
    t.checkExpect(
        new CountStudentAppearancesVisitor(s1)
            .apply(
                new ConsList<Course>(c4,
                    new ConsList<Course>(c3,
                        new ConsList<Course>(c2, new ConsList<Course>(c1, new MtList<Course>()))))),
        2);
    t.checkExpect(new CountStudentAppearancesVisitor(s2)
        .apply(new ConsList<Course>(c2, new ConsList<Course>(c1, new MtList<Course>()))), 0);
    reset();
  }

  // tests for SameStudent
  void testSameStudent(Tester t) {
    reset();
    t.checkExpect(new SameStudent(this.s1).apply(this.s1), true);
    t.checkExpect(new SameStudent(this.s1).apply(this.s2), false);
    t.checkExpect(new SameStudent(this.s1).apply(new Student("s1", 02, new MtList<Course>())),
        false);
    t.checkExpect(new SameStudent(this.s1).apply(new Student("s2", 01, new MtList<Course>())),
        false);
    reset();
  }

  // tests for dejavu
  void testDejavu(Tester t) {
    reset();
    t.checkExpect(this.i1.dejavu(this.s1), false);
    this.s1.enroll(this.c1);
    t.checkExpect(this.i1.dejavu(this.s1), false);
    this.s1.enroll(this.c2);
    t.checkExpect(this.i1.dejavu(this.s1), true);
    reset();
  }
}