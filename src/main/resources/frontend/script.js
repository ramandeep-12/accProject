// Global variables
let allCards = [];
let compareCards = [];
let searchHistory = [];

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
  fetchCards();
  loadSearchHistory();
});

// Navigation functions
function showSection(sectionName) {
  // Hide all sections
  const sections = document.querySelectorAll('.section');
  sections.forEach(section => section.classList.remove('active'));
  
  // Show the selected section
  const targetSection = document.getElementById(sectionName + '-section');
  if (targetSection) {
    targetSection.classList.add('active');
  }
  
  // Special handling for compare section
  if (sectionName === 'compare') {
    displayCompareCards();
  }
  
  // Close mobile menu
  const navLinks = document.querySelector('.nav-links');
  const hamburger = document.querySelector('.hamburger');
  navLinks.classList.remove('active');
  hamburger.classList.remove('active');
}

function toggleMenu() {
  const navLinks = document.querySelector('.nav-links');
  const hamburger = document.querySelector('.hamburger');
  navLinks.classList.toggle('active');
  hamburger.classList.toggle('active');
}

// Card fetching and display functions
async function fetchCards() {
  const search = document.getElementById("searchInput")?.value || "";
  
  // Add to search history if there's a search term
  if (search.trim()) {
    addToSearchHistory(search);
  }
  
  const url = `http://localhost:8080/api/creditcards?search=${encodeURIComponent(search)}`;

  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const cards = await response.json();
    allCards = cards;
    displayCards(cards);
  } catch (error) {
    console.error("Failed to fetch cards", error);
    const container = document.getElementById("cardsContainer");
    if (container) {
      container.innerHTML = `
        <div style="text-align: center; padding: 40px; color: #666;">
          <h3>Unable to load cards</h3>
          <p>Please check your connection and try again.</p>
          <button class="btn" onclick="fetchCards()">Retry</button>
        </div>
      `;
    }
  }
}

function displayCards(cards) {
  const container = document.getElementById("cardsContainer");
  if (!container) return;
  
  container.innerHTML = "";

  if (cards.length === 0) {
    container.innerHTML = "<p style='text-align: center; color: #666; font-size: 1.2em;'>No cards matched your search.</p>";
    return;
  }

  cards.forEach((card, index) => {
    const cardDiv = document.createElement("div");
    cardDiv.className = "card";
    cardDiv.setAttribute('data-card-id', index);

    cardDiv.innerHTML = `
      <img src="${card.cardImages || 'https://via.placeholder.com/300x180?text=Credit+Card'}" alt="${card.cardTitle}" onerror="this.src='https://via.placeholder.com/300x180?text=Credit+Card'" />
      <h3>${card.cardTitle}</h3>
      <p><strong>Bank:</strong> ${card.bankName}</p>
      <p><strong>Annual Fee:</strong> ${card.annualFees}</p>
      <p><strong>Interest:</strong> ${card.purchaseInterestRate}</p>
      <p><strong>Benefits:</strong> ${card.productBenefits}</p>
      <a href="${card.cardLink || '#'}" target="_blank">Apply Now</a>
    `;

    container.appendChild(cardDiv);
  });
}

// Compare functionality
function displayCompareCards() {
  const container = document.getElementById("compareCardsContainer");
  if (!container) return;
  
  container.innerHTML = "";

  if (allCards.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 40px; color: #666;">
        <h3>No cards available for comparison</h3>
        <p>Please load cards first by visiting the Explore section.</p>
        <button class="btn" onclick="showSection('explore')">Go to Explore</button>
      </div>
    `;
    return;
  }

  allCards.forEach((card, index) => {
    const cardDiv = document.createElement("div");
    cardDiv.className = "card";
    cardDiv.setAttribute('data-card-id', index);
    
    const isSelected = compareCards.some(c => c.index === index);
    if (isSelected) {
      cardDiv.classList.add('selected');
    }

    cardDiv.innerHTML = `
      <input type="checkbox" class="compare-checkbox" ${isSelected ? 'checked' : ''} 
             onchange="toggleCompareCard(${index})" ${compareCards.length >= 3 && !isSelected ? 'disabled' : ''}>
      <img src="${card.cardImages || 'https://via.placeholder.com/300x180?text=Credit+Card'}" alt="${card.cardTitle}" onerror="this.src='https://via.placeholder.com/300x180?text=Credit+Card'" />
      <h3>${card.cardTitle}</h3>
      <p><strong>Bank:</strong> ${card.bankName}</p>
      <p><strong>Annual Fee:</strong> ${card.annualFees}</p>
      <p><strong>Interest:</strong> ${card.purchaseInterestRate}</p>
      <p><strong>Benefits:</strong> ${card.productBenefits}</p>
      <a href="${card.cardLink || '#'}" target="_blank">Learn More</a>
    `;

    container.appendChild(cardDiv);
  });
  
  updateCompareCounter();
}

function toggleCompareCard(cardIndex) {
  const card = allCards[cardIndex];
  const existingIndex = compareCards.findIndex(c => c.index === cardIndex);
  
  if (existingIndex > -1) {
    // Remove from comparison
    compareCards.splice(existingIndex, 1);
  } else if (compareCards.length < 3) {
    // Add to comparison
    compareCards.push({ ...card, index: cardIndex });
  }
  
  updateCompareCounter();
  displayCompareCards();
}

function updateCompareCounter() {
  const counter = document.getElementById('compareCount');
  const showBtn = document.getElementById('showCompareBtn');
  
  if (counter) {
    counter.textContent = compareCards.length;
  }
  
  if (showBtn) {
    showBtn.disabled = compareCards.length < 2;
  }
}

function clearComparison() {
  compareCards = [];
  updateCompareCounter();
  displayCompareCards();
  document.getElementById('comparisonTable').style.display = 'none';
}

function showComparison() {
  if (compareCards.length < 2) {
    alert('Please select at least 2 cards to compare.');
    return;
  }
  
  const table = document.getElementById('comparisonTable');
  const headers = ['card1Header', 'card2Header', 'card3Header'];
  const tbody = document.getElementById('comparisonTableBody');
  
  // Set headers
  compareCards.forEach((card, index) => {
    const header = document.getElementById(headers[index]);
    if (header) {
      header.textContent = card.cardTitle;
      header.style.display = 'table-cell';
    }
  });
  
  // Hide unused headers
  for (let i = compareCards.length; i < 3; i++) {
    const header = document.getElementById(headers[i]);
    if (header) {
      header.style.display = 'none';
    }
  }
  
  // Build comparison rows
  const features = [
    { label: 'Bank', key: 'bankName' },
    { label: 'Annual Fee', key: 'annualFees' },
    { label: 'Interest Rate', key: 'purchaseInterestRate' },
    { label: 'Benefits', key: 'productBenefits' }
  ];
  
  tbody.innerHTML = '';
  
  features.forEach(feature => {
    const row = document.createElement('tr');
    row.innerHTML = `<td>${feature.label}</td>`;
    
    compareCards.forEach(card => {
      const cell = document.createElement('td');
      cell.textContent = card[feature.key] || 'N/A';
      row.appendChild(cell);
    });
    
    // Add empty cells for missing cards
    for (let i = compareCards.length; i < 3; i++) {
      const emptyCell = document.createElement('td');
      emptyCell.style.display = 'none';
      row.appendChild(emptyCell);
    }
    
    tbody.appendChild(row);
  });
  
  table.style.display = 'block';
  table.scrollIntoView({ behavior: 'smooth' });
}

// Word frequency checker
async function checkWordFrequency() {
  const word = document.getElementById("wordInput").value.trim();
  if (!word) {
    alert('Please enter a word to check frequency.');
    return;
  }

  try {
    const response = await fetch(`http://localhost:8080/api/creditcards/word-frequency?word=${encodeURIComponent(word)}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    document.getElementById("frequencyResult").textContent = `"${data.word}" appears ${data.count} time(s).`;
  } catch (err) {
    console.error("Failed to fetch word frequency:", err);
    document.getElementById("frequencyResult").textContent = "Error fetching frequency. Please try again.";
  }
}

// Recommendations functionality
function getRecommendations() {
  const primaryUse = document.getElementById('primaryUse').value;
  const feeRange = document.getElementById('feeRange').value;
  
  if (!primaryUse || !feeRange) {
    alert('Please fill in all fields to get recommendations.');
    return;
  }
  
  const recommendations = generateRecommendations(primaryUse, feeRange);
  displayRecommendations(recommendations);
}

function generateRecommendations(primaryUse, feeRange) {
  // Filter cards based on real backend data
  const filteredCards = allCards.filter(card => {
    let matchesFee = true;
    let matchesUse = true;
    
    // Fee filtering based on actual data
    const annualFee = card.annualFees?.toLowerCase() || '';
    switch (feeRange) {
      case 'free':
        matchesFee = annualFee.includes('$0') || annualFee.includes('free') || annualFee.includes('no fee') || annualFee === '$0' || annualFee === '0';
        break;
      case 'low':
        // Extract numeric value from fee string
        const feeMatch = annualFee.match(/\$(\d+)/);
        if (feeMatch) {
          const feeAmount = parseInt(feeMatch[1]);
          matchesFee = feeAmount > 0 && feeAmount <= 50;
        } else {
          matchesFee = !annualFee.includes('$0') && !annualFee.includes('free');
        }
        break;
      case 'medium':
        const mediumFeeMatch = annualFee.match(/\$(\d+)/);
        if (mediumFeeMatch) {
          const feeAmount = parseInt(mediumFeeMatch[1]);
          matchesFee = feeAmount > 50 && feeAmount <= 150;
        }
        break;
      case 'high':
        const highFeeMatch = annualFee.match(/\$(\d+)/);
        if (highFeeMatch) {
          const feeAmount = parseInt(highFeeMatch[1]);
          matchesFee = feeAmount > 150;
        }
        break;
    }
    
    // Use case filtering based on actual card data
    const benefitsText = card.productBenefits?.toLowerCase() || '';
    const titleText = card.cardTitle?.toLowerCase() || '';
    const bankText = card.bankName?.toLowerCase() || '';
    const combinedText = `${benefitsText} ${titleText} ${bankText}`;
    
    switch (primaryUse) {
      case 'cashback':
        matchesUse = combinedText.includes('cash') || combinedText.includes('back') || 
                     combinedText.includes('cashback') || combinedText.includes('rebate');
        break;
      case 'travel':
        matchesUse = combinedText.includes('travel') || combinedText.includes('miles') || 
                     combinedText.includes('points') || combinedText.includes('airline') ||
                     combinedText.includes('rewards');
        break;
      case 'balance':
        matchesUse = combinedText.includes('balance') || combinedText.includes('transfer') || 
                     combinedText.includes('low interest') || combinedText.includes('debt');
        break;
      case 'building':
        matchesUse = combinedText.includes('secured') || combinedText.includes('student') || 
                     combinedText.includes('building') || combinedText.includes('starter') ||
                     combinedText.includes('beginner');
        break;
    }
    
    return matchesFee && matchesUse;
  });
  
  // Sort by relevance (you could implement a scoring system here)
  return filteredCards.slice(0, 3); // Return top 3 recommendations
}

function displayRecommendations(recommendations) {
  const container = document.getElementById('recommendationsResult');
  
  if (recommendations.length === 0) {
    container.innerHTML = '<p style="text-align: center; color: #666;">No recommendations found based on your criteria. Try adjusting your preferences.</p>';
    return;
  }
  
  container.innerHTML = '<h3>Your Personalized Recommendations</h3>';
  
  const cardsHtml = recommendations.map((card, index) => `
    <div class="card" style="margin: 20px auto; max-width: 400px;">
      <div style="background: linear-gradient(45deg, #667eea, #764ba2); color: white; padding: 10px; border-radius: 10px 10px 0 0; text-align: center; font-weight: bold;">
        Recommendation #${index + 1}
      </div>
      <img src="${card.cardImages || 'https://via.placeholder.com/300x180?text=Credit+Card'}" alt="${card.cardTitle}" onerror="this.src='https://via.placeholder.com/300x180?text=Credit+Card'" />
      <h3>${card.cardTitle}</h3>
      <p><strong>Bank:</strong> ${card.bankName}</p>
      <p><strong>Annual Fee:</strong> ${card.annualFees}</p>
      <p><strong>Interest:</strong> ${card.purchaseInterestRate}</p>
      <p><strong>Benefits:</strong> ${card.productBenefits}</p>
      <a href="${card.cardLink || '#'}" target="_blank">Apply Now</a>
    </div>
  `).join('');
  
  container.innerHTML += cardsHtml;
}

// Search history functionality
function addToSearchHistory(searchTerm) {
  const timestamp = new Date().toLocaleString();
  const historyItem = {
    term: searchTerm,
    timestamp: timestamp,
    id: Date.now()
  };
  
  // Remove duplicate searches
  searchHistory = searchHistory.filter(item => item.term !== searchTerm);
  
  // Add to beginning of array
  searchHistory.unshift(historyItem);
  
  // Keep only last 10 searches
  if (searchHistory.length > 10) {
    searchHistory = searchHistory.slice(0, 10);
  }
  
  saveSearchHistory();
  updateHistoryDisplay();
}

function saveSearchHistory() {
  // In a real app, this would save to localStorage or send to server
  // For this demo, we'll just keep it in memory
}

function loadSearchHistory() {
  // In a real app, this would load from localStorage or server
  updateHistoryDisplay();
}

function updateHistoryDisplay() {
  const container = document.getElementById('historyContainer');
  if (!container) return;
  
  if (searchHistory.length === 0) {
    container.innerHTML = '<p>No search history available yet. Start exploring cards to see your history here!</p>';
    return;
  }
  
  const historyHtml = searchHistory.map(item => `
    <div class="history-item" style="background: rgba(255,255,255,0.9); padding: 15px; margin: 10px 0; border-radius: 10px; border-left: 4px solid #667eea; display: flex; justify-content: space-between; align-items: center;">
      <div>
        <strong>"${item.term}"</strong>
        <br>
        <small style="color: #666;">${item.timestamp}</small>
      </div>
      <button class="btn" style="padding: 5px 15px; margin: 0;" onclick="searchAgain('${item.term}')">Search Again</button>
    </div>
  `).join('');
  
  container.innerHTML = `
    <div style="margin-bottom: 20px;">
      <button class="btn" onclick="clearHistory()" style="background: #e74c3c;">Clear History</button>
    </div>
    ${historyHtml}
  `;
}

function searchAgain(term) {
  document.getElementById('searchInput').value = term;
  showSection('explore');
  fetchCards();
}

function clearHistory() {
  if (confirm('Are you sure you want to clear your search history?')) {
    searchHistory = [];
    updateHistoryDisplay();
  }
}

// Add these new functions
async function rankCardsByRelevance(cards) {
  try {
    const response = await fetch('http://localhost:8080/api/creditcards/rank', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(cards)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const rankedCards = await response.json();
    return rankedCards;
  } catch (error) {
    console.error("Failed to rank cards", error);
    alert("Failed to rank cards. Please try again.");
    return cards; // fallback to unranked
  }
}

// Re-rank cards based on backend ranking
async function applyPageRanking() {
  if (!allCards || allCards.length === 0) return;

  try {
    const ranked = await rankCardsByRelevance(allCards);
    displayCards(ranked);
  } catch (error) {
    console.error("Failed to apply ranking", error);
    alert("Failed to rank cards. Please try again.");
  }
}

// Add these functions
async function showPageRanking() {
  const search = document.getElementById("searchInput").value.trim();
  
  if (!search) {
    alert("Please enter a search term first");
    return;
  }
  
  try {
    // Fixed URL - removed extra parenthesis
    const response = await fetch(
      `http://localhost:8080/api/creditcards/page-ranking?term=${encodeURIComponent(search)}`
    );
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    displayPageRanking(data);
  } catch (error) {
    console.error("Failed to get page ranking", error);
    alert("Failed to get page ranking. Please try again.");
  }
}

function displayPageRanking(data) {
  const container = document.getElementById("cardsContainer");
  container.innerHTML = "";
  
  const header = document.createElement("div");
  header.className = "ranking-header";
  header.innerHTML = `
    <h2>Page Ranking for: "${data.searchTerm}"</h2>
    <p>Results sorted by relevance score</p>
  `;
  container.appendChild(header);
  
  const table = document.createElement("table");
  table.className = "ranking-table";
  table.innerHTML = `
    <thead>
      <tr>
        <th>Card</th>
        <th>Bank</th>
        <th>Relevance</th>
        <th>Occurrences</th>
        <th>Link</th>
      </tr>
    </thead>
    <tbody id="rankingBody"></tbody>
  `;
  
  const tbody = table.querySelector("#rankingBody");
  
  data.results.forEach(result => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${result.title}</td>
      <td>${result.bank}</td>
      <td>${result.relevance.toFixed(4)}</td>
      <td>${result.occurrences}</td>
      <td><a href="${result.url}" target="_blank">Apply Now</a></td>
    `;
    tbody.appendChild(row);
  });
  
  container.appendChild(table);
}
