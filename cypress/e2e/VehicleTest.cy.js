describe('Автомобильные круды', () => {

    beforeEach(() => {
        cy.login('manager1', 'password')
    })

    it("Создать новый автомобиль", () => {
        cy.get("a[href*='/vehicles?enterpriseId=1']").click();
        cy.contains('Добавить новый автомобиль').click();

        cy.get("select[name=modelId]").select("1");
        cy.get("select[name=enterpriseId]").select("1");
        cy.get("#licensePlateNumber").type("A000AA01");
        cy.get("#mileageKm").clear().type("1000");
        cy.get("#productionYear").clear().type("2015");
        cy.get("#color").type("White");
        cy.get("#purchasePriceRub").clear().type("1250000");
        cy.get("#purchaseDateTime").clear().type("2025-01-01T00:00");

        cy.get('form').submit();

        cy.url().should("include", "/vehicles");
        cy.get('.pagination li:last-child a').then(($btn) => {
            if ($btn.is(':enabled')) {
                cy.wrap($btn).click();
            } else {
                cy.log('Button is disabled, skipping click.');
            }
        });
        cy.contains("A000AA01");
    });

    it("Изменить автомобиль", () => {
        cy.get("a[href*='/vehicles?enterpriseId=1']").click();
        cy.get('.pagination li:last-child a').then(($btn) => {
            if ($btn.is(':enabled')) {
                cy.wrap($btn).click();
            } else {
                cy.log('Button is disabled, skipping click.');
            }
        });

        cy.contains('tr', 'A000AA01').within(() => {
            cy.contains('td', 'Black').should('not.exist');
            cy.get('a[href*="edit"]').click();
        });

        cy.get("#mileageKm").clear().type("2000");
        cy.get("#productionYear").clear().type("2020");
        cy.get("#color").clear().type("Black");
        cy.get("#purchaseDateTime").clear().type("2025-01-01T00:00");

        cy.get('form').submit();

        cy.get('.pagination li:last-child a').then(($btn) => {
            if ($btn.is(':enabled')) {
                cy.wrap($btn).click();
            } else {
                cy.log('Button is disabled, skipping click.');
            }
        });

        cy.contains('tr', 'A000AA01').within(() => {
            cy.contains('2000');
            cy.contains('2020');
            cy.contains('Black');
        });
    });

    it("Удалить автомобиль", () => {
        cy.get("a[href*='/vehicles?enterpriseId=1']").click();
        cy.url().should("include", "/vehicles");

        cy.get('.pagination li:last-child a').then(($btn) => {
            if ($btn.is(':enabled')) {
                cy.wrap($btn).click();
            } else {
                cy.log('Button is disabled, skipping click.');
            }
        });

        cy.contains('tr', 'A000AA01').within(() => {
            cy.get('a[href*="delete"]').click();
        });

        cy.get('.pagination li:last-child a').then(($btn) => {
            if ($btn.is(':enabled')) {
                cy.wrap($btn).click();
            } else {
                cy.log('Button is disabled, skipping click.');
            }
        });

        cy.contains('table tr', 'A000AA01').should('not.exist')
    });

})