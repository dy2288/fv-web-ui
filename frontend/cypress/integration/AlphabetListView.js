import 'cypress-testing-library/add-commands'
describe('AlphabetListView', () => {
  it('Default state: no message, no selected, no stop browsing button', () => {
    cy.visit('http://0.0.0.0:3001/nuxeo/app/explore/FV/sections/Data/Haisla/Haisla/Haisla/learn/words')
    const unselectedColor = 'rgb(60, 52, 52)'
    // No message, button, or selected letters
    cy.queryByText(/showing words that start with the letter/i).should('not.exist')
    cy.queryByText(/stop browsing alphabetically/i).should('not.exist')
    cy.getByTestId('AlphabetListView').within(() => {
      cy.get('a').each(($el) => {
        cy.wrap($el)
          .should('have.css', 'color')
          .and('eq', unselectedColor)
      })
    })

    // Now select a letter
    const clicked = 'x̄°'
    cy.getByText(clicked).click()

    // Is it highlighted?
    cy.getByTestId('AlphabetListView').within(() => {
      cy.getByText(clicked)
        .should('have.css', 'color')
        .and('not.eq', unselectedColor)
    })

    // Is the message and clear button displayed?
    cy.queryByText(/showing words that start with the letter/i).should('exist')
    cy.queryByText(/stop browsing alphabetically/i).should('exist')

    // Reset
    cy.queryByText(/stop browsing alphabetically/i).click()

    // Ensure all is back to normal...
    cy.queryByText(/showing words that start with the letter/i).should('not.exist')
    cy.queryByText(/stop browsing alphabetically/i).should('not.exist')
    cy.getByTestId('AlphabetListView').within(() => {
      cy.get('a').each(($el) => {
        cy.wrap($el)
          .should('have.css', 'color')
          .and('eq', unselectedColor)
      })
    })
  })
  it('Direct link: displays message, selected letter, & stop browsing buton', () => {
    cy.visit(
      'http://0.0.0.0:3001/nuxeo/app/explore/FV/sections/Data/Haisla/Haisla/Haisla/learn/words/alphabet/k%CC%93%C2%B0'
    )
    cy.getByText(/showing words that start with the letter/i).should('exist')
    cy.getByTestId('AlphabetListView').within(() => {
      cy.getByText('k̓°')
        .should('have.css', 'color')
        .and('eq', 'rgb(130, 0, 0)')
    })
    cy.getByText(/stop browsing alphabetically/i).should('exist')
  })
})