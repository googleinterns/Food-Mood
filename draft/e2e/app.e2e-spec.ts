import { FoodMoodPage } from './app.po';

describe('food-mood App', function() {
  let page: FoodMoodPage;

  beforeEach(() => {
    page = new FoodMoodPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
