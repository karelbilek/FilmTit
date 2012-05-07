#you first need to run mvn dependency:copy-dependencies to dependency to work
#(there is probably an easier way :))
scala -cp ./core/target/classes:./share/target/classes:./eval/target/classes:./userspace/target/classes:./core/target/dependency/*:./share/target/dependency/*:./eval/target/dependency/*:./userspace/target/dependency/*
