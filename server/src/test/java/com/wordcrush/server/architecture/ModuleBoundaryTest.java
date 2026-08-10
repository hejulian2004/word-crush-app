package com.wordcrush.server.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.wordcrush.server.WordCrushServerApplication;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packagesOf = WordCrushServerApplication.class)
class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule external_code_must_use_user_api = noClasses()
            .that().resideOutsideOfPackage("..module.user..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.user.account..",
                    "..module.user.avatar..");

    @ArchTest
    static final ArchRule external_code_must_use_learning_api = noClasses()
            .that().resideOutsideOfPackage("..module.learning..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.learning.controller..",
                    "..module.learning.dto..",
                    "..module.learning.entity..",
                    "..module.learning.repository..",
                    "..module.learning.response..",
                    "..module.learning.service..",
                    "..module.learning.support..");

    @ArchTest
    static final ArchRule external_code_must_use_game_api = noClasses()
            .that().resideOutsideOfPackage("..module.game..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.game.controller..",
                    "..module.game.dto..",
                    "..module.game.entity..",
                    "..module.game.repository..",
                    "..module.game.response..",
                    "..module.game.service..");

    @ArchTest
    static final ArchRule external_code_must_use_admin_api = noClasses()
            .that().resideOutsideOfPackage("..module.admin..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.admin.controller..",
                    "..module.admin.dto..",
                    "..module.admin.response..",
                    "..module.admin.service..");

    @ArchTest
    static final ArchRule learning_must_not_access_user_internals = noClasses()
            .that().resideInAnyPackage("..module.learning..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.user.account..",
                    "..module.user.avatar..");

    @ArchTest
    static final ArchRule game_must_not_access_user_internals = noClasses()
            .that().resideInAnyPackage("..module.game..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.user.account..",
                    "..module.user.avatar..");

    @ArchTest
    static final ArchRule admin_must_not_access_persistence_internals = noClasses()
            .that().resideInAnyPackage("..module.admin..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..module.user.account..",
                    "..module.user.avatar..",
                    "..module.learning.entity..",
                    "..module.learning.repository..");

    @ArchTest
    static final ArchRule controllers_must_not_access_persistence = noClasses()
            .that().resideInAnyPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..entity..", "..repository..");

    @ArchTest
    static final ArchRule common_must_not_depend_on_business_modules = noClasses()
            .that().resideInAnyPackage("..common..")
            .should().dependOnClassesThat().resideInAnyPackage("..module..", "..security..");

    @ArchTest
    static final ArchRule security_must_not_depend_on_business_modules = classes()
            .that().resideInAnyPackage("..security..")
            .should().onlyDependOnClassesThat()
            .resideOutsideOfPackages("..module.user.account..", "..module.learning..", "..module.game..", "..module.admin..");
}
