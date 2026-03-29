describe('Авторизация', () => {

    beforeEach(() => {
        cy.visit('/login')
    })

    it('Показать ошибку при вводе неверных данных', () => {
        cy.get('#username').type('wronguser')
        cy.get('#password').type('wrongpass')
        cy.get('button[type="submit"]').click()

        cy.contains('Неверный логин или пароль.')

        cy.url().should('include', '/login')
    })

    it('Пустить при вводе верных данных', () => {
        cy.get('#username').type('manager1')
        cy.get('#password').type('password')
        cy.get('button[type="submit"]').click()

        cy.url().should('eq', Cypress.config().baseUrl + '/enterprises')
    })
})